package LireApi;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;

import net.semanticmetadata.lire.imageanalysis.features.GlobalFeature;
import net.semanticmetadata.lire.imageanalysis.features.global.ColorLayout;
import net.semanticmetadata.lire.imageanalysis.features.global.EdgeHistogram;
import net.semanticmetadata.lire.imageanalysis.features.global.JCD;
import net.semanticmetadata.lire.imageanalysis.features.global.PHOG;
import net.semanticmetadata.lire.indexers.hashing.BitSampling;
import net.semanticmetadata.lire.utils.ImageUtils;

public class Features {

	private static Map<String, Class> listOfFeatures = new HashMap<String, Class>();
	private static Map<String, GlobalFeature> features = new HashMap<String, GlobalFeature>();
	private static int maxSideLength = 512;

	private static String arrayToString(int[] array) {
		StringBuilder sb = new StringBuilder(array.length * 8);
		for (int i = 0; i < array.length; i++) {
			if (i > 0) sb.append(' ');
			sb.append(Integer.toHexString(array[i]));
		}
		return sb.toString();
	}

	public static void load() throws IOException {
		BitSampling.readHashFunctions();
		listOfFeatures.clear();
		listOfFeatures.put("cl", ColorLayout.class);
		listOfFeatures.put("jc", JCD.class);
		listOfFeatures.put("ph", PHOG.class);
		listOfFeatures.put("eh", EdgeHistogram.class);

		features.clear();
		for (String key: listOfFeatures.keySet()) {
			try {
				features.put(key, (GlobalFeature) listOfFeatures.get(key).newInstance());
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}

	public static Map<String, String> get(BufferedImage img) {
		img = ImageUtils.trimWhiteSpace(img);
		BufferedImage wc_img = ImageUtils.createWorkingCopy(img);
		wc_img = ImageUtils.scaleImage(wc_img, maxSideLength);
		Map<String, String> res = new HashMap<String, String>();
		for (String key : features.keySet()) {
			GlobalFeature feature = features.get(key);
			feature.extract(wc_img);
			res.put(key + "_hi", Base64.getEncoder().encodeToString(feature.getByteArrayRepresentation()));
			res.put(key + "_ha", arrayToString(BitSampling.generateHashes(feature.getFeatureVector())));
		}
		return res;
	}
}
