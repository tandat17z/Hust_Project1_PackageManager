package model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Map;


public abstract class Root {
	public static String rootDir = "D:\\Hust_project1_PackageManager\\";
	String fileType;
	
	public static Root getInstance(String type) {
		if( type.equals("maven")) {
			return new RootMaven();
		}
		else if( type.equals("gradle")) {
			return new RootGradle();
		}
		else if( type.equals("npm")) {
			return new RootNpm();
		}
		return null;
	}
	
	public void copyToLocal(File configFile, String path) {
		try {
			Files.copy(
					configFile.toPath(), 
					new File(path, this.fileType).toPath(),
					StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public abstract void create();
	public abstract void saveDependencyTreeToTxt(String projectPath);
	public abstract Map<Library, Library> getDependency(String path);
	public abstract void downloadConfigFile(Library library);

	public String getFileType() {
		// TODO Auto-generated method stub
		return fileType;
	}
}
