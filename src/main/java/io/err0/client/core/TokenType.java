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

public enum TokenType {
    CONTENT,
    SOURCE_CODE,
    COMMENT_LINE,
    COMMENT_BLOCK,
    APOS_LITERAL,
    APOS3_LITERAL,
    QUOT_LITERAL,
    QUOT3_LITERAL,
    BACKTICK_LITERAL,
    LONGBRACKET_LITERAL,
}
