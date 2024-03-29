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

public interface AnalyseLogic {

    void pass2ResolveDuplicateErrorNumber(TokenStateItem item, long errorOrdinal);

    void pass2AssignNewErrorNumber(TokenStateItem item);

    void pass3AssignNewErrorNumber(Token currentToken);

    void pass3InsertExistingErrorNumber(StateItem stateItem, Token currentToken);

    void pass4CheckIfFileChanged(StateItem stateItem);

    void pass4ProcessResult(StateItem stateItem, String filename, SourceCodeParse parse);

    /**
     * @return true if a file changed, or should change
     */
    boolean returnValue();

}
