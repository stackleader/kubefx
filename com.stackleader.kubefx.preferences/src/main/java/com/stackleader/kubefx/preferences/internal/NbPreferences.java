/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package com.stackleader.kubefx.preferences.internal;

import com.stackleader.kubefx.preferences.PreferenceUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Radek Matous
 */
public class NbPreferences extends AbstractPreferences implements ChangeListener {

    private static final Logger LOG = LoggerFactory.getLogger(NbPreferences.class);
    private static Preferences USER_ROOT;
    private ThreadLocal<Boolean> localThread = new ThreadLocal<Boolean>();
    private List<String> keyEntries = new ArrayList<String>();
    private HashMap<String, ArrayList<String>> cachedKeyValues = new HashMap<String, ArrayList<String>>();

    private EditableProperties properties;
    private FileStorage fileStorage;

    private NbPreferences() {
        super(null, "");
        fileStorage = getFileStorage(absolutePath());
        fileStorage.attachChangeListener(this);
    }

    public static Preferences userRootImpl() {
        if (USER_ROOT == null) {
            USER_ROOT = new NbPreferences();
        }
        assert USER_ROOT != null;
        return USER_ROOT;
    }

    /**
     * Creates a new instance of PreferencesImpl
     */
    private NbPreferences(NbPreferences parent, String name) {
        super(parent, name);
        fileStorage = getFileStorage(absolutePath());
        newNode = !fileStorage.existsNode();
        fileStorage.attachChangeListener(this);
    }

    @Override
    protected AbstractPreferences childSpi(String name) {
        return new NbPreferences(this, name);
    }

    protected NbPreferences.FileStorage getFileStorage(String absolutePath) {
        // work with user files
        return new PropertiesStorage(PreferenceUtils.getPreferenceConfigDirectory(), absolutePath());
    }

    @Override
    protected final String getSpi(String key) {
        return getProperty(key);
    }

    private String getProperty(String key) {
        synchronized (lock) {
            return properties().getProperty(key);
        }
    }

    @Override
    protected final String[] childrenNamesSpi() throws BackingStoreException {
        //TODO: cache it if necessary
        return fileStorage.childrenNames();
    }

    @Override
    protected final String[] keysSpi() throws BackingStoreException {
        return getKeysSpi();
    }

    private String[] getKeysSpi() throws BackingStoreException {
        synchronized (lock) {
            Set<String> keySet = properties().keySet();
            return keySet.toArray(new String[keySet.size()]);
        }
    }

    @Override
    protected final void putSpi(String key, String value) {
        putProperty(key, value);
        if (Boolean.TRUE.equals(localThread.get())) {
            return;
        }
        fileStorage.markModified();
        asyncInvocationOfFlushSpi();
    }

    private void putProperty(String key, String value) {
        synchronized (lock) {
            properties().put(key, value);
        }
    }

    @Override
    public void put(String key, String value) {
        put(key, value, false);
    }

    public void put(String key, String value, boolean triggeredByStateChangedEvent) {
        String oldValue = getSpi(key);
        if (value.equals(oldValue)) {
            return;
        }
        try {
            if (super.isRemoved()) {
                return;
            }
            ArrayList<String> cachedValues = cachedKeyValues.get(key);
            if (cachedValues == null) {
                cachedValues = new ArrayList<String>();
            }
            if (triggeredByStateChangedEvent) {
                if (cachedValues.contains(value)) {
                    return;
                }
            } else {
                cachedValues.add(value);
                cachedKeyValues.put(key, cachedValues);
            }
            super.put(key, value);
        } catch (IllegalArgumentException iae) {
            if (iae.getMessage().contains("too long")) {
                // Not for us!
                putSpi(key, value);
            } else {
                throw iae;
            }
        }
    }

    @Override
    protected final void removeSpi(String key) {
        removeProperty(key);
        keyEntries.remove(key);
        cachedKeyValues.remove(key);
        if (Boolean.TRUE.equals(localThread.get())) {
            return;
        }
        fileStorage.markModified();
        asyncInvocationOfFlushSpi();
    }

    private void removeProperty(String key) {
        synchronized (lock) {
            properties().remove(key);
        }
    }

    @Override
    protected final void removeNodeSpi() throws BackingStoreException {
        try {
            fileStorage.removeNode();
        } catch (IOException ex) {
            throw new BackingStoreException(ex);
        }
    }

    private void asyncInvocationOfFlushSpi() {
        if (!fileStorage.isReadOnly()) {
            CompletableFuture.runAsync(() -> {
                try {
                    flushSpi();
                } catch (BackingStoreException ex) {
                    LOG.error(ex.getMessage(), ex);
                }
            }).whenComplete((Void v, Throwable ex) -> {
                if (ex != null) {
                    LOG.error(ex.getMessage(), ex);
                }
            });
        }
    }

    @Override
    protected void flushSpi() throws BackingStoreException {
        try {
            synchronized (lock) {
                fileStorage.save(properties());
            }
        } catch (IOException ex) {
            throw new BackingStoreException(ex);
        }
    }

    @Override
    protected void syncSpi() throws BackingStoreException {
        if (properties != null) {
            try {
                putAllProperties(fileStorage.load(), true);

            } catch (IOException ex) {
                throw new BackingStoreException(ex);
            }
        }
    }

    private EditableProperties properties() {
        if (properties == null) {
            properties = new EditableProperties(true);
            //properties.putAll(loadDefaultProperties());
            try {
                putAllProperties(fileStorage.load(), false);
            } catch (IOException ex) {
                LOG.error(ex.getMessage(), ex);
            }
        }
        return properties;
    }

    private void putAllProperties(EditableProperties props, boolean clear) {
        synchronized (lock) {
            if (clear) {
                properties().clear();
            }
            properties().putAll(props);
        }
    }

    public @Override
    final void removeNode() throws BackingStoreException {
        if (fileStorage.isReadOnly()) {
            throw new BackingStoreException("Unsupported operation: read-only storage");//NOI18N
        } else {
            if (super.isRemoved()) {
                return;
            }
            clearProperties();
            super.removeNode();
        }
    }

    private void clearProperties() {
        synchronized (lock) {
            properties().clear();
            keyEntries.clear();
            cachedKeyValues.clear();
        }
    }

    public @Override
    final void flush() throws BackingStoreException {
        if (fileStorage.isReadOnly()) {
            throw new BackingStoreException("Unsupported operation: read-only storage");//NOI18N
        } else {
            super.flush();
            cachedKeyValues.clear();
        }
    }

    public @Override
    final void sync() throws BackingStoreException {
        if (fileStorage.isReadOnly()) {
            throw new BackingStoreException("Unsupported operation: read-only storage");//NOI18N
        } else {
            if (super.isRemoved()) {
                return;
            }
            try {
                flushSpi();
            } catch (BackingStoreException ex) {
                LOG.error(ex.getMessage(), ex);
            }
            super.sync();
            cachedKeyValues.clear();
        }
    }

    public @Override
    void stateChanged(ChangeEvent e) {
        synchronized (lock) {
            Boolean previewState = localThread.get();
            EditableProperties ep = null;
            ArrayList<String> entries2add = new ArrayList<String>();
            try {
                localThread.set(Boolean.TRUE);
                ep = fileStorage.load();
                Iterator<Entry<String, String>> iter = ep.entrySet().iterator();
                while (iter.hasNext()) {
                    Entry entry = iter.next();
                    if (keyEntries.isEmpty() || keyEntries.contains(entry.getKey().toString())) {
                        put(entry.getKey().toString(), entry.getValue().toString(), true);
                        entries2add.add(entry.getKey().toString());
                    }
                }
            } catch (IOException ex) {
                LOG.error(ex.getMessage(), ex);
            } finally {
                ArrayList<String> entries2remove = new ArrayList<String>();
                for (String key : keyEntries) {
                    if (!entries2add.contains(key)) {
                        if (super.isRemoved()) {
                            continue;
                        }
                        entries2remove.add(key);
                    }
                }
                for (String key : entries2remove) {
                    remove(key);
                }
                keyEntries.clear();
                keyEntries.addAll(entries2add);
                localThread.set(previewState);
            }
        }
    }

    interface FileStorage {

        boolean isReadOnly();

        String[] childrenNames();

        boolean existsNode();

        void removeNode() throws IOException;

        void markModified();

        EditableProperties load() throws IOException;

        void save(final EditableProperties properties) throws IOException;

        void attachChangeListener(ChangeListener changeListener);

    }
}
