package LireApiWeb;

import LireApi.Features;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GetImgFeatures extends HttpServlet {

	private String featuresToXml(String id, String title, Map<String, String> features) {
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

	private String errorToXml(int errorCode, String message) {
		String xml = new String();
		xml += "<error>";
		xml += "<code>" + errorCode + "</code>";
		xml += "<message>" + message + "</message>";
		xml += "</error>";

		return xml;
	}

	public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

		res.setContentType("text/xml");
		res.setCharacterEncoding( "UTF-8" );
		PrintWriter out = res.getWriter();

		String url = req.getParameter("img");
		String title = req.getParameter("title");
		if(url == null || title == null) {
			res.setStatus(res.SC_BAD_REQUEST);
			out.println(errorToXml(res.SC_BAD_REQUEST, "Missing parameters ?img and/or ?title"));
			return;
		}

		BufferedImage img = null;
		URL urlImg = new URL(url);
		HttpURLConnection conn = (HttpURLConnection) urlImg.openConnection();
		conn.connect();
		long contentLength = Long.parseLong(conn.getHeaderField("Content-Length"));

		// > 1MB
		if(contentLength > 1048576) {
			res.setStatus(res.SC_BAD_REQUEST);
			out.println(errorToXml(res.SC_BAD_REQUEST, "Image is too large (> 1MB)"));
			return;
		}

		try {
			img = ImageIO.read(new URL(url));
		} catch (IOException e) {
			res.setStatus(res.SC_BAD_REQUEST);
			out.println(errorToXml(res.SC_BAD_REQUEST, "Cannot load requested image"));
			return;
		}
		Features.load();
		Map<String, String> features = Features.get(img);
		out.println(featuresToXml(url, title, features));
	}
}
