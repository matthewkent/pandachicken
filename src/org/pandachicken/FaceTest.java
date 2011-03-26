package org.pandachicken;

import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static com.googlecode.javacv.cpp.opencv_core.cvClearMemStorage;
import static com.googlecode.javacv.cpp.opencv_core.cvGetSeqElem;
import static com.googlecode.javacv.cpp.opencv_core.cvLoad;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_BGR2GRAY;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCvtColor;
import static com.googlecode.javacv.cpp.opencv_objdetect.cvHaarDetectObjects;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;

import javax.imageio.ImageIO;

import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.cpp.opencv_core.CvMemStorage;
import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.googlecode.javacv.cpp.opencv_core.CvSeq;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_objdetect;
import com.googlecode.javacv.cpp.opencv_objdetect.CvHaarClassifierCascade;

public class FaceTest {

	/**
	 * test out all the frontal face classifiers in opencv
	 */
	public static void main(String[] args) throws Exception {
		final String rootName = "chicken";
		File imageDir = new File("tmp/faces");
		File source = new File("images/" + rootName + ".jpg");
		for(File f: imageDir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.matches(rootName + "face-.*.jpg");
			}
		})) {
			f.delete();
		}
		// Preload the opencv_objdetect module to work around a known bug.
		Loader.load(opencv_objdetect.class);

		File dir = new File("/opt/local/share/opencv/haarcascades");
		for (File f : dir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.matches("haarcascade_frontalface_.*.xml");
			}
		})) {
			CvHaarClassifierCascade classifier = new CvHaarClassifierCascade(
					cvLoad(f.getCanonicalPath()));
			BufferedImage original = ImageIO.read(new FileInputStream(source));
			IplImage sourceImage = IplImage.createFrom(original);
			IplImage grayImage = IplImage.create(sourceImage.width(),
					sourceImage.height(), IPL_DEPTH_8U, 1);
			CvMemStorage storage = CvMemStorage.create();

			cvCvtColor(sourceImage, grayImage, CV_BGR2GRAY);
			CvSeq faces = cvHaarDetectObjects(grayImage, classifier, storage,
					1.1, 1, opencv_objdetect.CV_HAAR_DO_CANNY_PRUNING);
			int total = faces.total();
			System.out.println("Found " + total + " faces with classifier " + f.getName());
			for (int i = 0; i < total; i++) {
				CvRect r = new CvRect(cvGetSeqElem(faces, i));
				int x = r.x(), y = r.y(), w = r.width(), h = r.height();
				BufferedImage crop = original.getSubimage(x, y, w, h);
				ImageIO.write(crop, "jpg", new File(imageDir + "/" + rootName + "-" + f.getName() + "-" + i
						+ ".jpg"));
			}
			cvClearMemStorage(storage);
		}
	}

}
