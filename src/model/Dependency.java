package model;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Dependency {
	@Override
	public int hashCode() {
		return Objects.hash(artifactId, groupId, version);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Dependency other = (Dependency) obj;
		return Objects.equals(artifactId, other.artifactId) && Objects.equals(groupId, other.groupId)
				&& Objects.equals(version, other.version);
	}

	static String repoPath = "D:\\PomRepo\\";
	
	private String groupId;
	private String artifactId;
	private String version;
	
	public Dependency(String groupId, String artifactId, String version) {
		this.groupId = groupId;
		this.artifactId = artifactId;
		this.version = version;
	}
	
	@Override
	public String toString() {
		return groupId.replace('.', '/') + "/"
               + artifactId + "/" + version + "/" + artifactId + "-" + version;
	}
	
	public List<Dependency> readPom(String pomPath){
		List<Dependency> dependencies = new ArrayList<>();
		
		File file = new File(pomPath);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			Document document = builder.parse(file);
			NodeList dependencyNodes = document.getElementsByTagName("dependency");
			
			for (int i = 0; i < dependencyNodes.getLength(); i++) {
				Element dependencyElement = (Element) dependencyNodes.item(i);
				
				String groupId = dependencyElement.getElementsByTagName("groupId").item(0).getTextContent();
				
				String artifactId = dependencyElement.getElementsByTagName("artifactId").item(0).getTextContent();
				
				NodeList versionElement = dependencyElement.getElementsByTagName("version");
				
//				NodeList classifier = dependencyElement.getElementsByTagName("classifier");
				NodeList scopeElement = dependencyElement.getElementsByTagName("classifier");
				NodeList classifierElement = dependencyElement.getElementsByTagName("scope");
				
				if(scopeElement.getLength() != 0 && scopeElement.item(0).getTextContent() == "test") {
					continue;
				}
				if(classifierElement.getLength() != 0) {
					continue;
				}
				String version;
				if( versionElement.getLength() == 0) {
					version = "";
				}
				else {
					version = versionElement.item(0).getTextContent();
				}
                dependencies.add(new Dependency(groupId, artifactId, version));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return dependencies;
	}
	
	public List<Dependency> getDependency(){
		String pomUrl = "https://repo1.maven.org/maven2/" + toString() + ".pom";
		try {
			String pomPath = downloadPom();
			return readPom(pomPath);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private String downloadPom() throws Exception {
		String pomUrl = "https://repo1.maven.org/maven2/" + toString() + ".pom";
        HttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(pomUrl);

        HttpResponse response = httpClient.execute(httpGet);
        HttpEntity entity = response.getEntity();

        if (entity != null) {
            String pomContent =  EntityUtils.toString(entity);
            String pomPath = repoPath + toString().replace('/', '\\') + "-pom.xml";
         
            try {
            	
                saveToFile(pomPath, pomContent);
//                System.out.println("POM file downloaded successfully.");
            } catch (Exception e) {
            	System.out.println("Failed to download POM file.");
                e.printStackTrace();
            }
            return pomPath;
        }

        throw new Exception("Failed to download POM file.");
    }
	
	private static void saveToFile(String pomPath, String pomContent){
        File file = new File(pomPath);
        File parentDir = file.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }
        
        try {
            // Tạo và ghi vào file
            FileWriter writer = new FileWriter(file);
            writer.write(pomContent);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	public void getClassifier() {
		
	}
}
