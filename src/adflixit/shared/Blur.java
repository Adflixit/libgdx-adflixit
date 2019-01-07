/*
 * Copyright 2018 Adflixit
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package adflixit.shared;

import static adflixit.shared.TweenUtils.*;
import static aurelienribon.tweenengine.TweenCallback.*;

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.equations.Quart;
import aurelienribon.tweenengine.primitives.MutableFloat;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

/**
 * Performs two-pass Gaussian blur.
 */
public class Blur extends ScreenComponent<BaseScreen<?>> {
  private final String        uniName = "u_blur";
  private ShaderProgram       hpass;
  private ShaderProgram       vpass;
  private FrameBuffer         fb;
  private FrameBuffer         hfb;
  private FrameBuffer         vfb;
  private int                 passes  = 1;  // number of blurring cycles
  private final MutableFloat  amount  = new MutableFloat(0);
  private boolean             pass;       // update route permit
  private boolean             scheduled;  // one-time update route permit

  public Blur(BaseScreen<?> screen) {
    super(screen);
  }

  public Blur(BaseScreen<?> screen, int passes) {
    super(screen);
    setPasses(passes);
  }

  public Blur(BaseScreen<?> screen, FileHandle hvert, FileHandle hfrag, FileHandle vvert, FileHandle vfrag) {
    super(screen);
    load(hvert, hfrag, vvert, vfrag);
  }

  public Blur(BaseScreen<?> screen, String hvert, String hfrag, String vvert, String vfrag) {
    super(screen);
    load(hvert, hfrag, vvert, vfrag);
  }

  public void load(FileHandle hvert, FileHandle hfrag, FileHandle vvert, FileHandle vfrag) {
    hpass = new ShaderProgram(hvert, hfrag);
    vpass = new ShaderProgram(vvert, vfrag);
  }

  public void load(String hvert, String hfrag, String vvert, String vfrag) {
    hpass = new ShaderProgram(hvert, hfrag);
    vpass = new ShaderProgram(vvert, vfrag);
  }

  public Blur setPasses(int i) {
    passes = i;
    return this;
  }

  public void reset() {
    resetAmount();
    lock();
    unschedule();
  }

  public void draw() {
    Texture tex;
    float x = scr.cameraX0(), y = scr.cameraY0();
    for (int i=0; i < passes; i++) {
      pass(i, x, y);
    }
    bat.setShader(null);
    bat.begin();
      tex = vfb.getColorBufferTexture();
      bat.draw(tex, x, y, scr.screenWidth(), scr.screenHeight(), 0,0,1,1);
    bat.end();
    if (scheduled) {
      unschedule();
    }
  }

  public void begin() {
    fb.begin();
  }

  public void end() {
    fb.end();
    draw();
  }

  public Texture inputTex() {
    return fb.getColorBufferTexture();
  }

  /** Performs the blurring routine.
   * @param i iterations
   * @param x result drawing x
   * @param y result drawing y */
  private void pass(int i, float x, float y) {
    Texture tex;
    // horizontal pass
    bat.setShader(hpass);
    hfb.begin();
    bat.begin();
      if (pass || scheduled) {
        hpass.setUniformf(uniName, amount());
      }
      tex = i > 0 ? vfb.getColorBufferTexture() : inputTex();
      bat.draw(tex, x, y, scr.screenWidth(), scr.screenHeight());
    bat.end();
    hfb.end();
    // vertical pass
    bat.setShader(vpass);
    vfb.begin();
    bat.begin();
      if (pass || scheduled) {
        vpass.setUniformf(uniName, amount());
      }
      tex = hfb.getColorBufferTexture();
      bat.draw(tex, x, y, scr.screenWidth(), scr.screenHeight());
    bat.end();
    vfb.end();
  }

  /** Locks the shader update route. */
  private void lock() {
    pass = false;
  }

  /** Unlocks the shader update route. */
  private void unlock() {
    pass = true;
  }

  /** Schedules a one-time access to the update route. */
  private void schedule() {
    scheduled = true;
  }

  /** Resets the one-time access to the update route. */
  private void unschedule() {
    scheduled = false;
  }

  public boolean isActive() {
    return amount() > 0;  
  }

  public float amount() {
    return amount.floatValue();
  }

  public void setAmount(float v) {
    killTweenTarget(amount);
    amount.setValue(v);
    schedule();
  }

  public void resetAmount() {
    setAmount(0);
  }

  /** @param v value
   * @param d duration */
  public Tween $tween(float v, float d) {
    killTweenTarget(amount);
    return Tween.to(amount, 0, d).target(v).ease(Quart.OUT)
           .setCallback((type, source) -> {
             if (type==BEGIN) {
               unlock();
             } else {
               lock();
             }
           })
           .setCallbackTriggers(BEGIN|COMPLETE);
  }

  /** @param d duration */
  public Tween $tweenOut(float d) {
    return $tween(0, d);
  }

  /** @param v value */
  public Tween $setAmount(float v) {
    killTweenTarget(amount);
    return Tween.set(amount, 0).target(v).setCallback((type, source) -> schedule());
  }

  public Tween $resetAmount() {
    return $setAmount(0);
  }

  public void dispose() {
    hpass.dispose();
    vpass.dispose();
    hfb.dispose();
    vfb.dispose();
  }

  public void resize() {
    if (firstResize) {
      firstResize = false;
    } else {
      fb.dispose();
      hfb.dispose();
      vfb.dispose();
    }
    fb = new FrameBuffer(Format.RGB888, scr.fbWidth(), scr.fbHeight(), false);
    hfb = new FrameBuffer(Format.RGB888, scr.fbWidth(), scr.fbHeight(), false);
    vfb = new FrameBuffer(Format.RGB888, scr.fbWidth(), scr.fbHeight(), false);
  }
}
