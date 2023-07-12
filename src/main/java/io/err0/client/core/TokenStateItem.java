/*
Copyright 2023 ERR0 LLC

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package io.err0.client.core;

public class TokenStateItem {
    TokenStateItem(final StateItem stateItem, final Token token, final long inputErrorOrdinal) {
        this.stateItem = stateItem;
        this.token = token;
        this.inputErrorOrdinal = inputErrorOrdinal;
    }

    public final StateItem stateItem;
    public final Token token;
    public final long inputErrorOrdinal;
    public int nMatchFromMethod = 0;
    public int nMatchFromFile = 0;
}
