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

import com.google.common.io.Files;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import static java.util.stream.Collectors.toList;
import java.util.stream.StreamSupport;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * No synchronization - must be called just from NbPreferences which
 * ensures proper synchronization
 *
 * @author Radek Matous
 */
class PropertiesStorage implements NbPreferences.FileStorage {

    private final String folderPath;
    private String filePath;
    private boolean isModified;
    private static final Logger LOG = LoggerFactory.getLogger(PropertiesStorage.class);
    private final File configRoot;
    private static Object lock = new Object();
    private final ScheduledExecutorService scheduler;

    /**
     * Creates a new instance
     */
    public PropertiesStorage(final File configRoot, final String absolutePath) {
        StringBuilder sb = new StringBuilder();
        sb.append(configRoot.getPath());
        sb.append(absolutePath);
        folderPath = sb.toString();
        filePath();
        this.configRoot = configRoot;
        scheduler = Executors.newScheduledThreadPool(1);
    }

    public boolean isReadOnly() {
        return false;
    }

    public void markModified() {
        isModified = true;
    }

    public final boolean existsNode() {
        return (toPropertiesFile() != null) || (toFolder() != null);
    }

    public String[] childrenNames() {
        File folder = toFolder();
        List<String> folderNames = new ArrayList<String>();

        if (folder != null) {
            List<File> childFolders = StreamSupport.stream(Files.fileTreeTraverser().children(folder).spliterator(), false)
                    .filter(child -> child.isDirectory())
                    .collect(toList());
            
            for (File fo : childFolders) {
                Files.fileTreeTraverser().breadthFirstTraversal(fo).filter(child -> Files.getFileExtension(child.getPath()).equals("properties")).forEach(child -> folderNames.add(fo.getName()));
            }
                        
//            List<File> childFiles = StreamSupport.stream(Files.fileTreeTraverser().children(folder).spliterator(), false)
//                    .filter(child -> child.isFile())
//                    .collect(toList());
//            
//            for (File fo : childFiles) {
//                if (Files.getFileExtension(fo.getPath()).equals("properties")) { // NOI18N
//                    folderNames.add(fo.getName());
//                }
//            }
        }

        return folderNames.toArray(new String[folderNames.size()]);
    }

    public final void removeNode() throws IOException {
        File propertiesFile = toPropertiesFile();
        if (propertiesFile != null) {
            propertiesFile.delete();
            File folder = propertiesFile.getParentFile();
            while (folder != null && !folder.equals(configRoot) && getChildCount(folder) == 0) {
                folder.delete();
                folder = folder.getParentFile();
            }
        }
    }

    public EditableProperties load() throws IOException {
        EditableProperties retval = new EditableProperties(true);
        File file = toPropertiesFile();
        if (file != null) {
            try {
                InputStream is = new FileInputStream(file);
                try {
                    retval.load(is);
                } finally {
                    is.close();
                }
            } catch (IllegalArgumentException ex) { // #167745
                LOG.error(ex.getMessage(), ex);
                file.delete();
            }
        }
        return retval;
    }

    public void save(final EditableProperties properties) throws IOException {
        if (isModified) {
            isModified = false;
            if (!properties.isEmpty()) {
                OutputStream os = null;
                try {
                    os = outputStream();
                    properties.store(os);
                } finally {
                    if (os != null) {
                        os.close();
                    }
                }
            } else {

                File file = toPropertiesFile();
                if (file != null) {
                    file.delete();
                }
                File folder = toFolder();

                while (folder != null && !folder.equals(configRoot) && getChildCount(folder) == 0) {
                    folder.delete();
                    folder = folder.getParentFile();
                }
            }
        }
    }

    private int getChildCount(File file) {
        int count = 0;
        Iterable<File> children = Files.fileTreeTraverser().children(file);
        for (File child : children) {
            count++;
        }
        return count;
    }

    private OutputStream outputStream() throws IOException {
        File fo = toPropertiesFile();
        return new FileOutputStream(fo);
    }

    private String folderPath() {
        return folderPath;
    }

    private String filePath() {
        if (filePath == null) {
            String[] all = folderPath().split("/");//NOI18N
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < all.length; i++) {
                sb.append(all[i]).append("/");//NOI18N
            }
            if (all.length > 0) {
                sb.append(all[all.length - 1]).append(".properties");//NOI18N
            } else {
                sb.append("root.properties");//NOI18N
            }
            filePath = sb.toString();
        }
        return filePath;
    }

    protected File toFolder() {
        String safeFolderPath = folderPath.replace("/", File.separator);
        File folder = new File(safeFolderPath);
        try {
            Files.createParentDirs(folder);
            folder.mkdir();
        } catch (IOException ex) {
            LOG.error(ex.getMessage(), ex);
        }
        return folder;
    }

    protected File toPropertiesFile() {
        String safeFilePath = filePath().replace("/", File.separator);
        File propFile = new File(safeFilePath);
        try {
            Files.createParentDirs(propFile);
            Files.touch(propFile);
        } catch (IOException ex) {
            LOG.error(ex.getMessage(), ex);
        }
        return propFile;
    }

    @Override
    public void attachChangeListener(final ChangeListener changeListener) {
        try {
            final Path propFolderPath = toFolder().toPath();
            WatchService watcher = propFolderPath.getFileSystem().newWatchService();
            propFolderPath.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
            scheduler.schedule(() -> {
                try {
                    while (true) {
                        WatchKey watckKey = watcher.take();
                        List<WatchEvent<?>> events = watckKey.pollEvents();
                        for (WatchEvent event : events) {
                            File fe = null;
                            if (event.kind() instanceof Path) {
                                fe = Path.class.cast(event.kind()).toFile();
                            } else {
                                return;
                            }
                            if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                                if (fe.equals(toPropertiesFile())) {
                                    changeListener.stateChanged(new ChangeEvent(PropertiesStorage.this));
                                }
                            }
                            if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
                                if (fe.equals(toPropertiesFile())) {
                                    changeListener.stateChanged(new ChangeEvent(PropertiesStorage.this));
                                }
                            }
                            if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                                if (fe.equals(toPropertiesFile())) {
                                    changeListener.stateChanged(new ChangeEvent(PropertiesStorage.this));
                                }
                            }
                        }
                        watckKey.reset();
                    }
                } catch (InterruptedException ex) {
                    LOG.error(ex.getMessage(), ex);
                }
            }, 0, TimeUnit.SECONDS);

        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }

}
