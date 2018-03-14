package sample.daos;

import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import java.util.Locale;
import java.util.Scanner;
class AddingImagesRun{
    public void run() {
        double alpha = 0.5; double beta; double input;
        Mat src1, src2, dst = new Mat();
        System.out.println(" Simple Linear Blender ");
        System.out.println("-----------------------");
        System.out.println("* Enter alpha [0.0-1.0]: ");
        Scanner scan = new Scanner( System.in ).useLocale(Locale.US);
        input = scan.nextDouble();
        if( input >= 0.0 && input <= 1.0 )
            alpha = input;
        src1 = Imgcodecs.imread("/home/muhamamddanish/Pictures/images/shirt.jpg");
        src2 = Imgcodecs.imread("/home/muhamamddanish/Pictures/images/sanmarshirt.jpg");
        if( src1.empty() == true ){ System.out.println("Error loading src1"); return;}
        if( src2.empty() == true ){ System.out.println("Error loading src2"); return;}
        beta = ( 1.0 - alpha );
        Core.addWeighted( src1, alpha, src2, beta, 0.0, dst);

        HighGui.imshow("Linear Blend", dst);
        HighGui.waitKey(0);
        System.exit(0);
    }
}
public class AddingImages {
    public static void main(String[] args) {
        // Load the native library.
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        new AddingImagesRun().run();



            /*if (map.type() != target.type()) {
                return;
            } else {
                if (map.cols() == target.cols()) {
                    croppedImage = cropImage(target);
                    dst = new Mat();
                    roi = new Rect(100, 200, croppedImage.cols(), croppedImage.rows());
                    imageROI = map.submat(roi);
                    Core.addWeighted(imageROI, 0.3, croppedImage, 1 - 0.3, 0.0, dst);
                    dst.copyTo(imageROI);

                    // HighGui.imshow("Linear Blend", backgroundImageMatrics);
                    //  HighGui.waitKey(0);
                    imageView.setImage(SwingFXUtils.toFXImage(toBufferImage(map), null));
                    return;

                } else {
                    roi = new Rect(565, 706, target.cols(), target.rows());
                    gray_src = new Mat();

                    if (0 <= roi.x
                            && 0 <= roi.width
                            && roi.x + roi.width <= map.cols()
                            && 0 <= roi.y
                            && 0 <= roi.height
                            && roi.y + roi.height <= map.rows()) {
                        dst = new Mat();
                        imageROI = map.submat(roi);

                        Core.addWeighted(imageROI, 0.5, target, 0.5, 0.0, dst);
                        dst.copyTo(imageROI);
                       *//* imageView.setImage(SwingFXUtils.toFXImage(toBufferImage(map), null));
                        return;*//*
                        HighGui.imshow("Overlay Using Transparency", map);
                        HighGui.waitKey(0);
                        CloseApp();
                    }
                    dst = new Mat();
                    croppedImage = cropImage(target);
                    roi = new Rect(106, 360, croppedImage.cols(), croppedImage.rows());
                    imageROI = map.submat(roi);
                    Core.addWeighted(imageROI, 0.3, croppedImage, 1 - 0.3, 0.0, dst);
                    dst.copyTo(imageROI);
                    *//*HighGui.imshow("Overlay Using Transparency", map);
                    HighGui.waitKey(0);*//*
                    imageView.setImage(SwingFXUtils.toFXImage(toBufferImage(map), null));

                }*/



    }
}