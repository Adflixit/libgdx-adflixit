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

package adflixit.shared.tests.app;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;

import adflixit.shared.BaseScreen;

public class TestAppScreen extends BaseScreen<TestApp> {
	public TestAppScreen() {
		super();
		game = new TestApp();
	}

	@Override public void goBack() {
	}

	public static void launch(TestAppScreen screen) {
		new LwjglApplication(screen.game, "Test App", 360, 640);
	}
}