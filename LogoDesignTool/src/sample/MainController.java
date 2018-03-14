package sample;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseDragEvent;
import javafx.stage.FileChooser;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.highgui.HighGui;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.BackgroundSubtractorMOG2;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.opencv.video.Video;


public class MainController implements Initializable {
    Mat gray_src, detected_edges, dst, cdst, cdstP;
    Mat src;
    int edgeThresh = 1;
    int lowThreshold;
    final int max_lowThreshold = 100;
    int ratio = 3;
    int kernel_size = 3;
    @FXML
    CheckBox applyEdge;

    @FXML
    Button selectImageBtn;

    @FXML
    Button loadLogoBtn;

    @FXML
    ImageView imageView;

    @FXML
    Slider cannyThreshold;

    private BufferedImage img = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        cannyThreshold.valueProperty().addListener((observableValue, oldValue, newValue) -> {

            System.out.println("Threshold : " + newValue);


        });
    }

    @FXML
    public void CloseApp() {
        Platform.exit();
        System.exit(0);
    }

    @FXML
    public void selectImageBtnAction(ActionEvent actionEvent) throws IOException {
        FileChooser fileChooser = new FileChooser();
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            img = ImageIO.read(selectedFile.toURL());
            imageView.setImage(SwingFXUtils.toFXImage(img, null));
        } else {
            System.out.println("File is not supported");
        }

    }

    @FXML
    public void cannySelected(ActionEvent event) {
        if (this.applyEdge.isSelected()) {
            src = createMat(img);
            if (src.empty()) {
                System.out.println("src data is empty");
            } else {
                dst = new Mat();
                Imgproc.Canny(src, dst, 50, 200, 3, false);
                // Copy edges to the images that will display the results in BGR
                cdst = new Mat();
                Imgproc.cvtColor(dst, cdst, Imgproc.COLOR_GRAY2BGR);
                cdstP = cdst.clone();
                //detect lines using houghP
                Mat lines = new Mat();
                Imgproc.HoughLinesP(dst, lines, 1, Math.PI / 180, 50, 50, 10); // runs the actual detection
                // Draw the lines
                for (int x = 0; x < lines.rows(); x++) {
                    double[] l = lines.get(x, 0);
                    Imgproc.line(cdstP, new Point(l[0], l[1]), new Point(l[2], l[3]), new Scalar(0, 0, 255), 3, Imgproc.LINE_AA, 0);
                }
                imageView.setImage(SwingFXUtils.toFXImage(toBufferImage(cdstP), null));

            }
        } else {
            imageView.setImage(SwingFXUtils.toFXImage(img, null));
        }
    }

    @FXML
    public void onSliderAction(MouseDragEvent event) {
        System.out.println("Working");
    }


    @FXML
    public void loadLogo(ActionEvent actionEvent) throws IOException, InterruptedException {
        Mat imageROI, croppedImage, lines;
        Mat target4C = new Mat(), map4C = new Mat();
        Rect roi;
        double alpha = 0.5;
        double beta;
        double input;

        FileChooser fileChooser = new FileChooser();
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            BufferedImage logoImage = ImageIO.read(selectedFile.toURL());
            Image logo = logoImage;
            Mat map = createMat(img);
            Mat target = createMat(logoImage);

            if (map.empty()) {
                System.out.println("Background image not found!");
            }
            System.out.println("Map size: " + map.cols() + "x" + map.rows() + " channels:" + map.channels() + " type:" + map.type());

            if (target.empty()) {
                System.out.println("Foreground image  not found!");
            }
            System.out.println("Target size: " + target.cols() + "x" + target.rows() + " channels:" + target.channels() + " type:" + target.type());


            if (target.channels() != 4) {
                Imgproc.cvtColor(target, target4C, Imgproc.COLOR_BGR2BGRA);
                System.out.println("A PNG image with transparent layer is required");

            }
            if (map.channels() != 4) {
                Imgproc.cvtColor(map, map4C, Imgproc.COLOR_BGR2BGRA);
                System.out.println("A PNG image with transparent layer is required");

            }

            if (target.size().width > map.size().width || target.size().height > map.size().height) {
                System.out.println("!!! Target needs to have smaller dimensions than map");
            }

            //Display the map as movie clip

            int offset_x = 0;
            Mat output = new Mat(target4C.size(), target4C.type()), output2 = new Mat();
            Mat frame = new Mat(), croppedMapRoi = new Mat();
            Rect roi1;
            try {
                if (target.channels() != 4 && !target4C.empty() && target4C.channels() == 4) {
                    System.out.println("Target4C size: " + target.cols() + "x" + target4C.rows() + " channels:" + target4C.channels() + " type:" + target4C.type());
                    System.out.println("Go Ahead");
                    roi1 = new Rect(offset_x, 0, (int) target4C.size().width, (int) target4C.size().height);
                    croppedMapRoi = map.submat(roi1);


                    displacementMapFilter(croppedMapRoi, target4C, 2, 2, 20, 20, output);
                } else {
                    roi1 = new Rect(offset_x, 0, (int) target.size().width, (int) target.size().height);
                    croppedMapRoi = map4C.submat(roi1);
                    displacementMapFilter(croppedMapRoi, target4C, 2, 2, 20, 20, output);
                }


                // Display the results on the screen
                Point point = new Point(0, 0);
                overlayImage(map, output, frame, point);
                Mat map2 = new Mat();
                Imgproc.cvtColor(output, output2, Imgproc.COLOR_RGBA2BGR);
                //imageView.setImage(SwingFXUtils.toFXImage(toBufferImage(map2), null));
                HighGui.imshow("OpenCV - Displacement Map Filter", output2);
                HighGui.waitKey(0);
            } catch (Exception e) {
                e.printStackTrace();
            }


        }
    }

    public Mat cropImage(Mat image) {
        Size size = new Size(100, 100);
        Mat resizeImage = new Mat();
        Imgproc.resize(image, resizeImage, size);
        return resizeImage;
    }

    public Mat blendMats(Mat backMat, Mat logoMat) {
        Mat mat = new Mat(backMat.rows(), backMat.cols() + logoMat.cols(), logoMat.rows());
        int backCols = backMat.cols();
        int backRows = backMat.rows();

        backMat = mat.rowRange(0, backRows - 1).colRange(0, backCols - 1);
        logoMat = mat.rowRange(0, backRows - 1).colRange(backCols, (backCols * 2) - 1);

        return mat;
    }

    public Mat createMat(BufferedImage image) {
        byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        Mat mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
        mat.put(0, 0, pixels);
        return mat;
    }

    public BufferedImage toBufferImage(Mat mat) {

        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (mat.channels() > 1) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }

        byte[] bytes = new byte[mat.channels() * mat.cols() * mat.rows()];
        mat.get(0, 0, bytes);
        BufferedImage image = new BufferedImage(mat.cols(), mat.rows(), type);
        final byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(bytes, 0, pixels, 0, bytes.length);
        return image;
    }

    public void overlayImage(final Mat background, final Mat foreground, Mat output, Point location) {
        try {
            background.copyTo(output);

            // start at the row indicated by location, or at row 0 if location.y is negative.
            for (int y = (location.y > 0) ? (int) location.y : 0; y < background.rows(); ++y) {
                int fY = y - (int) location.y; // because of the translation

                // we are done of we have processed all rows of the foreground image.
                if (fY >= foreground.rows())
                    break;

                // start at the column indicated by location, or at column 0 if location.x is negative.
                for (int x = (location.x > 0) ? (int) location.x : 0; x < background.cols(); ++x) {
                    int fX = x - (int) location.x; // because of the translation.

                    // we are done with this row if the column is outside of the foreground image.
                    if (fX >= foreground.cols())
                        break;

                    // determine the opacity of the foregrond pixel, using its fourth (alpha) channel.
                    // double opacity = ((double)foreground.data[fY * foreground.step + fX * foreground.channels() + 3]) / 255.;
                    double opacity = foreground.get(fY, fX)[2] / 255.;


                    // and now combine the background and foreground pixel, using the opacity, but only if opacity > 0.
                    for (int c = 0; opacity > 0 && c < output.channels(); ++c) {
                        double backgroundPx = background.get(fY, fX)[2] / 255. + c;
                        double foregroundPx = foreground.get(fY, fX)[2] / 255. + c;
                        output.put(y, x, ((backgroundPx * (1. - opacity)) + (foregroundPx * opacity)));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getComponent(double[] bgr_pixel, int bgr_component) {
        if (bgr_pixel == null) {
            return 0;
        } else {

            switch (bgr_component) {
                case 0: // Blue
                    return (int) bgr_pixel[0];

                case 1: // Green
                    return (int) bgr_pixel[1];

                case 2: // Red
                    return (int) bgr_pixel[2];

                default:
                    System.out.println("!!! getComponent: " + bgr_component + " is not a valid component");
                    break;
            }

            return 0;

        }
    }


    public void displacementMapFilter(final Mat map, final Mat target, int componentX, int componentY, int scaleX, int scaleY, Mat output) {
        if (componentX < 0 || componentX > 2 || componentY < 0 || componentY > 2) {
            System.out.println("!!! displacementMapFilter: componentX and componentY values must be in range [0,2]");
            return;
        }
        if (target.size().width != map.size().width || target.size().height != map.size().height || target.type() != CvType.CV_8UC4) {
            System.out.println("!!! displacementMapFilter: map and target need to have the same dimensions, and target type must be CV_8UC4");
            return;
        }
        // output.create(target.rows(), target.cols(), target.type());
        int opixels = (int) output.total() * (int) output.elemSize();
        byte[] outputPixels = new byte[opixels];
        int tpixels = (int) target.total() * (int) target.elemSize();
        byte[] targetPixels = new byte[tpixels];


//
// work with pixels
//
        for (int x = 0; x < output.rows(); x++) {
            for (int y = 0; y < output.cols(); y++) {

                /* Formula:
                *  dstPixel[x, y] = srcPixel[x + ((componentX(x, y) - 128) * scaleX) / 256,
                *                            y + ((componentY(x, y) - 128) * scaleY) / 256)]
                */
                int dx, dy;

                int numChannels = map.channels();//is 3 for 8UC3 (e.g. RGB)
                int frameSize = map.rows() * map.cols();
                int numChannels2 = map.channels();//is 3 for 8UC3 (e.g. RGB)
                int frameSize2 = output.rows() * map.cols();
                byte[] mapByteBuffer = new byte[frameSize * numChannels];
                byte[] byteBuffer = new byte[frameSize * numChannels];
                byte[] outputByteBuffer = new byte[frameSize * numChannels];

                dx = x + (getComponent(map.get(x, y), componentX) - 128) * scaleX / 256;
                if (dx < 0) dx = 0;
                if (dx >= output.rows()) dx = output.rows();

                dy = y + (getComponent(map.get(x, y), componentY) - 128) * scaleY / 256;
                if (dy < 0) dy = 0;
                if (dy >= output.cols()) dy = output.cols();

                //output.at<cv::Vec4b>(x, y) = target.at<cv::Vec4b>(dx, dy);
                // target.put(dx, dy, mapByteBuffer);
                //output.put(x, y, target.get(dx, dy));
                output.put(x, y, target.get(dx, dy));
            }

        }
    }
}



