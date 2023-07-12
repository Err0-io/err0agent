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

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicBoolean;

public class GlobalState {

    public final HashMap<Long, Signature> previousRunSignatures = new HashMap<>();
    public final HashMap<Long, ArrayList<TokenStateItem>> errorCodeMap = new HashMap<>();
    public final HashMap<String, StateItem> files = new HashMap<>();

    public void store(final String fullPathName, final String localToCheckoutUnchanged, final String localToCheckoutLower, final SourceCodeParse parse, final Charset fileCharset) {
        this.files.putIfAbsent(fullPathName, new StateItem(localToCheckoutUnchanged, localToCheckoutLower, parse, fileCharset));
    }

    /**
     * Store this error number, during initial classification pass.
     * @param errorOrdinal
     * @param stateItem
     * @param token
     * @return true if added a new errorOrdinal to the storage, false if this already exists.
     */
    public boolean store(final long errorOrdinal, final StateItem stateItem, final Token token)
    {
        final AtomicBoolean addedNew = new AtomicBoolean(false);
        errorCodeMap.compute(errorOrdinal, (k,v)->{
            if (v == null) {
                v = new ArrayList<>();
                addedNew.set(true);
            }
            v.add(new TokenStateItem(stateItem, token, errorOrdinal));
            return v;
        });
        return addedNew.get();
    }
}
