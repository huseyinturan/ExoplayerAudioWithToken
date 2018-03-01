/*
 * Copyright 2017 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.huseyinturan.exoplayeraudiotoken;

/**
 * Allows {@link ExoPlayerHolder} to report media playback duration and progress updates to
 * the {@link MainActivity}.
 */
public abstract class PlaybackInfoListener {

    void onDurationChanged(int duration) {
    }

    void onBufferingChanged(int position) {
    }

    void onPositionChanged(int position) {
    }

    void onError(String message) {
    }

    void onPlayingStarted() {
    }

    void onPlaying() {
    }

    void onLoading() {
    }

    void onPaused() {
    }

    void onClosed() {
    }
}


