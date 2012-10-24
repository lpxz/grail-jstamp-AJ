package Yada.java;

public class coordinate {

    double x;

    double y;

    public coordinate() {
    }

    static int coordinate_compare(coordinate aPtr, coordinate bPtr) {
        if (aPtr.x < bPtr.x) {
            return -1;
        } else if (aPtr.x > bPtr.x) {
            return 1;
        } else if (aPtr.y < bPtr.y) {
            return -1;
        } else if (aPtr.y > bPtr.y) {
            return 1;
        }
        return 0;
    }

    static double coordinate_distance(coordinate coordinatePtr, coordinate aPtr) {
        double delta_x = coordinatePtr.x - aPtr.x;
        double delta_y = coordinatePtr.y - aPtr.y;
        return Math.sqrt((delta_x * delta_x) + (delta_y * delta_y));
    }

    static double coordinate_angle(coordinate aPtr, coordinate bPtr, coordinate cPtr) {
        double delta_b_x;
        double delta_b_y;
        double delta_c_x;
        double delta_c_y;
        double distance_b;
        double distance_c;
        double numerator;
        double denominator;
        double cosine;
        double radian;
        delta_b_x = bPtr.x - aPtr.x;
        delta_b_y = bPtr.y - aPtr.y;
        delta_c_x = cPtr.x - aPtr.x;
        delta_c_y = cPtr.y - aPtr.y;
        numerator = (delta_b_x * delta_c_x) + (delta_b_y * delta_c_y);
        distance_b = coordinate_distance(aPtr, bPtr);
        distance_c = coordinate_distance(aPtr, cPtr);
        denominator = distance_b * distance_c;
        cosine = numerator / denominator;
        radian = Math.acos(cosine);
        return (180.0 * radian / 3.141592653589793238462643);
    }

    void coordinate_print() {
        System.out.print("(" + x + ", " + y + ")");
    }
}
