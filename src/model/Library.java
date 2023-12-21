package model;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import database.MavenProjectRoot;

public class Library extends MavenProjectRoot {
	String pomPath = "D:\\Hust_project1_PackageManager\\Library\\";
	String pomUrl = "https://repo1.maven.org/maven2/";
	
	String groupId;
	String artifactId;
	String version;
	
//	public static void main(String[] args) {
//		Library library = new Library("org.openjfx", "javafx-controls", "21.0.1");
//		library.save();
//	}
	
	public void save() {
		// TODO Auto-generated method stub
		File file = new File(pomPath);
        File parentDir = file.getParentFile();
        if ( parentDir.exists() ) {
        	System.out.println( toString() + " existed");
        	return;
        }
		saveDb();
		saveLocal();
	}

	@Override
	public String toString() {
		return groupId.replace('.', '/') + "/"
               + artifactId + "/" + version + "/" + artifactId + "-" + version;
	}

	public Library(String groupId, String artifactId, String version) {
		this.groupId = groupId;
		this.artifactId = artifactId;
		this.version = version;
		
		this.pomPath += groupId.replace('.', '\\') + "\\" + artifactId + "\\" + version;
		this.pomUrl += toString() + ".pom";
	}
	
	public void saveDb() {
        // Sửa thành kiểm tra dữ liệu trong database
        
		String sql = "INSERT INTO LIBRARY(groupid, artifactid, version) "
				+ "VALUES (?, ?, ?);";
		String[] arg = {groupId, artifactId, version};
		try {
			sqlModify(sql, arg);
			System.out.println("Lưu thành công Library trong db");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("Lưu Library trong db FAIL --------");
			e.printStackTrace();
		}
	}
	
	public void saveLocal() {
        downloadPom();
        dependencyTree(pomPath);
	}
	
	private void downloadPom() {
		File folder = new File(pomPath);
		folder.mkdirs();
		
        HttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(pomUrl);

		try {
			HttpResponse response;
			response = httpClient.execute(httpGet);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				String pomContent =  EntityUtils.toString(entity);
				saveToFile(pomPath + "\\pom.xml", pomContent);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    }
	
	private void saveToFile(String path, String pomContent){
        File file = new File(path);

        try {
            // Tạo và ghi vào file
            FileWriter writer = new FileWriter(file);
            writer.write(pomContent);
            writer.close();
            System.out.println("Download successful " + toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
}
