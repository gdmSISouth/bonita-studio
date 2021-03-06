/**
 * Copyright (C) 2011 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.studio.repository.themes;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bonitasoft.studio.common.FileUtil;
import org.bonitasoft.studio.common.ProjectUtil;
import org.bonitasoft.studio.common.log.BonitaStudioLog;
import org.bonitasoft.studio.common.platform.tools.PlatformUtil;
import org.bonitasoft.studio.common.jface.FileActionDialog;
import org.bonitasoft.studio.common.repository.Repository;
import org.bonitasoft.studio.common.repository.model.IRepositoryFileStore;
import org.bonitasoft.studio.common.repository.store.AbstractRepositoryStore;
import org.bonitasoft.studio.pics.Pics;
import org.bonitasoft.studio.pics.PicsConstants;
import org.bonitasoft.studio.repository.themes.i18n.Messages;
import org.bonitasoft.theme.ThemeDescriptorManager;
import org.bonitasoft.theme.css.CSSManager;
import org.bonitasoft.theme.exception.ThemeDescriptorNotFoundException;
import org.bonitasoft.theme.impl.ThemeDescriptorManagerImpl;
import org.bonitasoft.theme.model.ThemeDescriptor;
import org.bonitasoft.theme.model.ThemeType;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.emf.edapt.migration.MigrationException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

/**
 * @author Romain Bioteau
 * @author Baptiste Mesta
 * 
 */
public class LookNFeelRepositoryStore extends AbstractRepositoryStore<LookNFeelFileStore> {

    public static final String LEGACY_THEME_EXTENSION = ".looknfeel";
    public static final String LF_EXTENSION = "zip";

    private static String STORE_NAME = "looknfeels";

    private final CSSManager cssManager = CSSManager.getInstance();
    private final ThemeDescriptorManager themeDescriptorManager = ThemeDescriptorManagerImpl.getInstance();

    static final String UTF_8 = "UTF-8";
    static final String THEME_SERVLET_URL = "themeResource?theme=";
    static final String THEME_SERVLET_LOCATION = "&location=";
    private static final String THEME_SERVLET_URL_REGEX = "themeResource\\?theme=";
    public static final String THEME_SERVLET_TIMESTAMP = "&timestamp=";


    @Override
    public String getName() {
        return STORE_NAME ;
    }

    @Override
    public String getDisplayName() {
        return Messages.themeRepository;
    }

    @Override
    public Image getIcon() {
        return Pics.getImage(PicsConstants.looknfeel);
    }


    public CSSManager getCSSManager() {
        return cssManager;
    }

    public ThemeDescriptorManager getThemeDescriptorManager() {
        return themeDescriptorManager;
    }

    @Override
    public LookNFeelFileStore doImportInputStream(String fileName,InputStream inputStream) {
        final String looknfeelName = fileName.substring(0,fileName.length() - LF_EXTENSION.length() - 1);
        final IFolder parentFolder = getResource().getFolder(looknfeelName);

        LookNFeelFileStore artifcat = getChild(looknfeelName) ;
        if(artifcat != null && artifcat.isProvided()){
            Display.getDefault().syncExec(new Runnable() {

                @Override
                public void run() {
                    MessageDialog.openError(Display.getDefault().getActiveShell(), Messages.impossibleToOverwriteTitle, Messages.impossibleToOverwriteMsg) ;
                }
            }) ;
            return null ;
        }

        try{
            if (parentFolder.exists()) {
                if (FileActionDialog.overwriteQuestion(looknfeelName)) {
                    PlatformUtil.delete(parentFolder.getLocation().toFile(), Repository.NULL_PROGRESS_MONITOR);
                    parentFolder.refreshLocal(IResource.DEPTH_INFINITE, Repository.NULL_PROGRESS_MONITOR) ;
                    parentFolder.create(true, true, Repository.NULL_PROGRESS_MONITOR);
                    refresh() ;
                    extractThemeFromInputStream(parentFolder, inputStream);
                    replaceUrl(parentFolder);
                }
            } else {
                parentFolder.create(true, true, Repository.NULL_PROGRESS_MONITOR);
                extractThemeFromInputStream(parentFolder, inputStream);
                replaceUrl(parentFolder);
            }
        }catch (Exception e) {
            BonitaStudioLog.error(e) ;
            return null ;
        }finally{
        	if(inputStream != null){
        		try {
					inputStream.close();
				} catch (IOException e) {
					BonitaStudioLog.error(e);
				}
        	}
        }
        return createRepositoryFileStore(looknfeelName);
    }


    private void extractThemeFromInputStream(final IFolder parentFolder, final InputStream is) throws IOException, CoreException {
        final File tmpZipFile = new File(ProjectUtil.getBonitaStudioWorkFolder(), "themeTmpFile.zip");
        tmpZipFile.createNewFile();
        final FileOutputStream out = new FileOutputStream(tmpZipFile);
        FileUtil.copy(is, out);
        try {
			PlatformUtil.unzipZipFiles(tmpZipFile, parentFolder.getLocation().toFile(), Repository.NULL_PROGRESS_MONITOR);
		} catch (Exception e) {
			BonitaStudioLog.error(e);
		}
        out.close() ;
        parentFolder.refreshLocal(IResource.DEPTH_INFINITE, Repository.NULL_PROGRESS_MONITOR);
        tmpZipFile.delete();
    }



    @Override
    public List<LookNFeelFileStore> getChildren() {
        List<LookNFeelFileStore> result =  super.getChildren();
        Enumeration<URL> looknfeels = ThemeRepositoryPlugin.getDefault().getBundle().findEntries(STORE_NAME, "*", false);
        if(	looknfeels != null ){
            while (looknfeels.hasMoreElements()) {
                URL url = looknfeels.nextElement();
                LookNFeelFileStore f = createRepositoryFileStore(url.toString());
                if(f != null){
                    result.add(f) ;
                }
            }
        }

        return result;
    }

    @Override
    public LookNFeelFileStore getChild(String fileName) {
        LookNFeelFileStore file = null;
        IFolder folder = getResource().getFolder(fileName) ;
        if(!folder.isSynchronized(IResource.DEPTH_ONE) && folder.isAccessible()){
            try {
                folder.refreshLocal(IResource.DEPTH_ONE, Repository.NULL_PROGRESS_MONITOR) ;
            } catch (CoreException e) {
                BonitaStudioLog.error(e) ;
            }
        }
        if(folder.exists()){
            file = createRepositoryFileStore(fileName) ;
        }

        if(file == null){
            URL url = ThemeRepositoryPlugin.getDefault().getBundle().getResource(STORE_NAME+ "/" +fileName);
            if(url != null){
                return createRepositoryFileStore(url.toString()) ;
            }else{
                return null ;
            }
        }else{
            return file ;
        }

    }


    private void replaceUrl(final IResource resource) throws CoreException, IOException {
        replaceUrl( resource, "", 0, true);
    }

    private void replaceUrl(final IResource resource, final String baseFolder, int depth, final boolean firstCall) throws CoreException, IOException {
        if (resource.getType() == IResource.FOLDER) {
            final IFolder folder = (IFolder) resource;
            String baseFolder2;
            if (firstCall) {
                baseFolder2 = baseFolder;
            } else {
                baseFolder2 = baseFolder + resource.getName() + "/";
                depth++;
            }

            for (final IResource f : folder.members()) {
                replaceUrl(f, baseFolder2, depth, false);
            }
        } else if (resource.getType() == IResource.FILE
                && (resource.getName().endsWith(".html")
                        || resource.getName().endsWith(".htm")
                        || resource.getName().endsWith(".xhtml")
                        || resource.getName().endsWith(".css")
                        || resource.getName().endsWith(".jsp"))) {
            final IFile file = (IFile)resource;
            final InputStream fis = file.getContents();
            final String content = PlatformUtil.getFileContent(fis);
            fis.close();
            String newContent = new String(content);
            final StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(THEME_SERVLET_URL_REGEX);
            stringBuilder.append("[^ ]+");
            stringBuilder.append("(");
            stringBuilder.append(THEME_SERVLET_TIMESTAMP);
            stringBuilder.append("[0-9]+");
            stringBuilder.append(")?");
            stringBuilder.append(THEME_SERVLET_LOCATION);
            stringBuilder.append("([^\\)\"]*)");
            final String regex = stringBuilder.toString();
            final Pattern pattern = Pattern.compile(regex.toString());
            final Matcher matcher = pattern.matcher(newContent);
            final StringBuffer stringBuffer = new StringBuffer();
            while(matcher.find()) {
                final String url = matcher.group(2);
                if ((!(url.startsWith("http://") || url.startsWith("https://") || url.startsWith("file://") || url.startsWith("ftp://")))) {
                    StringBuilder builder = new StringBuilder();
                    for (int i = 0; i < depth; i++) {
                        builder.append("../");
                    }
                    builder.append(URLDecoder.decode(url, UTF_8));
                    matcher.appendReplacement(stringBuffer, builder.toString());
                }
            }
            matcher.appendTail(stringBuffer);
            newContent = stringBuffer.toString();
            file.setContents(new ByteArrayInputStream(newContent.getBytes(UTF_8)), IResource.FORCE, Repository.NULL_PROGRESS_MONITOR);

        }
    }

    @Override
    public LookNFeelFileStore createRepositoryFileStore(String fileName) {
        IFolder root = getResource().getFolder(fileName) ;
        if(root.exists()){
            try {
                root.refreshLocal(IResource.DEPTH_ONE, Repository.NULL_PROGRESS_MONITOR) ;
            } catch (CoreException e1) {
                BonitaStudioLog.error(e1) ;
            }
            IFile descriptor = root.getFile(ThemeDescriptorManager.THEME_DESCRIPTOR_NAME) ;
            if(descriptor.exists()){
                try {
                    ThemeDescriptor themeDescriptor = getThemeDescriptorManager().getThemeDescriptor(descriptor.getLocation().toFile()) ;
                    if(themeDescriptor.getType() == ThemeType.application){
                        return new ApplicationLookNFeelFileStore(fileName, this) ;
                    }else if(themeDescriptor.getType() == ThemeType.userXP){
                        return new UserXpFileStore(fileName, this) ;
                    }
                } catch (ThemeDescriptorNotFoundException e) {
                    BonitaStudioLog.error(e) ;
                }
            }else{
                try {
                    ThemeDescriptor themeDescriptor = getThemeDescriptorManager().createThemeDescriptor(fileName, descriptor.getLocation().toFile()) ;
                    themeDescriptor.setType(ThemeType.application);
                    return new ApplicationLookNFeelFileStore(fileName, this) ;
                } catch (IOException e) {
                    BonitaStudioLog.error(e);
                }
            }
        }else{
            try {
                URL url = new URL(fileName) ;
                if(url != null){
                    String path = FileLocator.toFileURL(url).getFile() ;
                    File descriptor = new File(path,ThemeDescriptorManager.THEME_DESCRIPTOR_NAME) ;
                    if(descriptor.exists()){
                        try {
                            ThemeDescriptor themeDescriptor = getThemeDescriptorManager().getThemeDescriptor(descriptor) ;
                            if(themeDescriptor.getType() == ThemeType.application){
                                return new URLApplicationLookNFeelFileStore(url, this) ;
                            }else if(themeDescriptor.getType() == ThemeType.userXP){
                                return new URLUserXPFileStore(url, this) ;
                            }
                        } catch (ThemeDescriptorNotFoundException e) {
                            BonitaStudioLog.error(e) ;
                        }
                    }
                }
            } catch (MalformedURLException e) {
                //            	ApplicationLookNFeelFileStore fileStore =  new ApplicationLookNFeelFileStore(fileName, this) ;
                //            	fileStore.save(null);
            	//BonitaStudioLog.error(e) ;
                return null;
            }catch (Exception e1) {
            	BonitaStudioLog.error("Exception when searching for file with name: "+ fileName, "org.bonitasoft.studio.repository.themes");
                BonitaStudioLog.error(e1) ;
                return null ;
            }
        }
        return null;
    }

    @Override
    public Set<String> getCompatibleExtensions() {
        return null;
    }


    public List<ApplicationLookNFeelFileStore> getApplicationLookNFeels(){
        List<ApplicationLookNFeelFileStore> result =  new ArrayList<ApplicationLookNFeelFileStore>() ;
        for(IRepositoryFileStore file : getChildren()){
            if(file instanceof ApplicationLookNFeelFileStore){
                result.add((ApplicationLookNFeelFileStore) file) ;
            }
        }
        return result;
    }

    public List<UserXpFileStore> getUserXPLookNFeels(){
        List<UserXpFileStore> result =  new ArrayList<UserXpFileStore>() ;
        for(IRepositoryFileStore file : getChildren()){
            if(file instanceof UserXpFileStore){
                result.add((UserXpFileStore) file) ;
            }
        }
        return result;
    }

    public LookNFeelFileStore duplicateFrom(final String name, final String newDescription, final LookNFeelFileStore duplicateFrom){
        if(name == null || name.isEmpty()){
            throw new IllegalArgumentException("name can't be null or empty");
        }
        if(duplicateFrom == null){
            throw new IllegalArgumentException("duplicateFrom can't be null");
        }

        refresh() ;
        File root = duplicateFrom.getRootFile() ;

        if(getChild(name) != null || getResource().getFolder(name).exists()){
            return null ;
        }


        PlatformUtil.copyResource(getResource().getLocation().append(name).toFile(), root, Repository.NULL_PROGRESS_MONITOR) ;
        refresh() ;

        LookNFeelFileStore file = createRepositoryFileStore(name) ;
        try {
            final ThemeDescriptor themeDescriptor = file.getThemeDescriptor();
            themeDescriptor.setCreationDate(System.currentTimeMillis());
            themeDescriptor.setName(name);
            themeDescriptor.setDescription(newDescription);
            themeDescriptor.setProvided(false);
            getThemeDescriptorManager().updateThemeDescriptor(themeDescriptor);
            refresh() ;
        } catch (final Exception e) {
            BonitaStudioLog.error(e);
        }

        return file ;
    }
    
    @Override
    public void migrate() throws CoreException, MigrationException {
    	// Nothing Todo
    }
}
