package sample;

public class RGB {
        double r, g, b;
        public RGB (double r, double g, double b) {
            this.r = r;
            this.g = g;
            this.b = b;
        }
        public RGB (int rgb) {
            r = (double) (rgb >> 16 & 0xff) / 255;
            g = (double) (rgb >> 8 & 0xff) / 255;
            b = (double) (rgb >> 0 & 0xff) / 255;
        }
        public void scale (double scale) {
            r *= scale;
            g *= scale;
            b *= scale;
        }
        public void add (RGB texel) {
            r += texel.r;
            g += texel.g;
            b += texel.b;
        }
        public int toRGB () {
            return 0xff000000 | (int) (r * 255.99) << 16 |
                    (int) (g * 255.99) << 8 | (int) (b * 255.99) << 0;
        }
}

