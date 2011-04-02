package org.pandachicken;

import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static com.googlecode.javacv.cpp.opencv_core.cvClearMemStorage;
import static com.googlecode.javacv.cpp.opencv_core.cvGetSeqElem;
import static com.googlecode.javacv.cpp.opencv_core.cvLoad;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_BGR2GRAY;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCvtColor;
import static com.googlecode.javacv.cpp.opencv_objdetect.cvHaarDetectObjects;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.image.BufferedImage;

import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.cpp.opencv_core.CvMemStorage;
import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.googlecode.javacv.cpp.opencv_core.CvSeq;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_objdetect;
import com.googlecode.javacv.cpp.opencv_objdetect.CvHaarClassifierCascade;

public class Slapper {

	private CvHaarClassifierCascade classifier; 

	public Slapper() {
		// Preload the opencv_objdetect module to work around a known bug.
		Loader.load(opencv_objdetect.class);
		
		classifier = new CvHaarClassifierCascade(
				cvLoad("/opt/local/share/opencv/haarcascades/haarcascade_frontalface_alt2.xml"));
	}
	
	/*
	 * Slaps face from source onto dest.
	 */
	public BufferedImage slap(BufferedImage source, BufferedImage dest) {
		CvRect sourceFace = findFace(source);
		CvRect destFace = findFace(dest);
		
		// 1. crop source face
		BufferedImage faceCrop = source.getSubimage(sourceFace.x(), sourceFace.y(), sourceFace.width(), sourceFace.height());

		Graphics2D g;

		// 2. resize source face
		BufferedImage resizedSourceFace = new BufferedImage(destFace.width(), destFace.height(), BufferedImage.TYPE_INT_RGB);
		g = resizedSourceFace.createGraphics();
		g.drawImage(faceCrop, 0, 0, destFace.width(), destFace.height(), null);
		g.dispose();

		
		// 3. draw dest image into output
		BufferedImage output = new BufferedImage(dest.getWidth(), dest.getHeight(), BufferedImage.TYPE_INT_RGB);
		g = output.createGraphics();
		g.drawImage(dest, 0, 0, dest.getWidth(), dest.getHeight(), null);
		
		// 4. draw source face onto dest
		Rectangle bounds = new Rectangle(destFace.x(), destFace.y(), destFace.width(), destFace.height());
		TexturePaint tp = new TexturePaint(resizedSourceFace, bounds);
		g.setPaint(tp);
		g.fillOval(destFace.x(), destFace.y(), destFace.width(), destFace.height());
		g.dispose();
		
		return output;
	}

	private CvRect findFace(BufferedImage image) {
		IplImage sourceImage = IplImage.createFrom(image);
		IplImage grayImage = IplImage.create(sourceImage.width(), sourceImage.height(), IPL_DEPTH_8U, 1);
		CvMemStorage storage = CvMemStorage.create();

		cvCvtColor(sourceImage, grayImage, CV_BGR2GRAY);
		CvSeq faces = cvHaarDetectObjects(grayImage, classifier, storage, 1.1, 1, opencv_objdetect.CV_HAAR_DO_CANNY_PRUNING);
		
		cvClearMemStorage(storage);

		return new CvRect(cvGetSeqElem(faces, faces.total() - 1));
	}

}
