package atest;

import org.json.JSONObject;

import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Test {
	public static void main(String[] args) {
        String packageJsonPath = "D:\\Hust_project1_PackageManager\\package.json"; // Thay đổi đường dẫn tới package.json của bạn

        try {
            String content = new String(Files.readAllBytes(Paths.get(packageJsonPath)));
            JSONObject packageJson = new JSONObject(content);

            Graph<String, String> dependencyGraph = buildDependencyGraph(packageJson);

            // In ra cây phụ thuộc
            System.out.println("Dependency Tree:");
            System.out.println(dependencyGraph.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Graph<String, String> buildDependencyGraph(JSONObject packageJson) {
        Graph<String, String> dependencyGraph = new DirectedSparseGraph<>();

        // Thêm gói chính vào đồ thị
        String mainPackage = packageJson.getString("name");
        dependencyGraph.addVertex(mainPackage);

        // Thêm phụ thuộc vào đồ thị
        addDependenciesToGraph(mainPackage, packageJson.getJSONObject("dependencies"), dependencyGraph);
        addDependenciesToGraph(mainPackage, packageJson.getJSONObject("devDependencies"), dependencyGraph);

        return dependencyGraph;
    }

    private static void addDependenciesToGraph(String parent, JSONObject dependencies, Graph<String, String> graph) {
        dependencies.keySet().forEach(dependency -> {
            // Thêm phụ thuộc vào đồ thị
            graph.addVertex(dependency);
            graph.addEdge(parent + " -> " + dependency, parent, dependency);

            // Nếu dependency có các phụ thuộc, đệ quy để thêm vào đồ thị
            if (dependencies.get(dependency) instanceof JSONObject) {
                addDependenciesToGraph(dependency, (JSONObject) dependencies.get(dependency), graph);
            }
        });
    }
}
