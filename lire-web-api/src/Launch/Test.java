package Launch;

import LireApi.Features;

import javax.imageio.ImageIO;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

public class Test {

	public static String featuresToXml(String id, String title, Map<String, String> features) {
		String xml = new String();
		xml += "<add><doc>";
		xml += "<field name=\"id\">" + id + "</field>";
		xml += "<field name=\"title\">" + title + "</field>";
		for(String key: features.keySet()){
			xml += "<field name=\"" + key + "\">" + features.get(key) + "</field>";
		}
		xml += "</doc></add>";

		return xml;
	}

	public static void main(String[] args) throws IOException {

		try {
			URL url = new URL("http://www.sofoot.com/d-445111-casillas.jpg");
			BufferedImage img = ImageIO.read(url);
			Features.load();
			Map<String, String> features = Features.get(img);
			System.out.println(featuresToXml("id", "title", features));
		} catch (IOException e) {
			System.out.println("error");
		}
	}
}
