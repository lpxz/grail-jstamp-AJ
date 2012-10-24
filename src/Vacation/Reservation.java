package Vacation;

public class Reservation {

    public Reservation(int id, int numTotal, int price) {
        this.id = id;
        this.numUsed = 0;
        this.numFree = numTotal;
        this.numTotal = numTotal;
        this.price = price;
        checkReservation();
    }

    int id;

    int numUsed;

    int numFree;

    int numTotal;

    int price;

    public void checkReservation() {
        int numUsed = this.numUsed;
        int numFree = this.numFree;
        int numTotal = this.numTotal;
        int price = this.price;
    }

    boolean reservation_addToTotal(int num) {
        if (numFree + num < 0) {
            return false;
        }
        numFree += num;
        numTotal += num;
        checkReservation();
        return true;
    }

    public boolean reservation_make() {
        if (numFree < 1) {
            return false;
        }
        numUsed++;
        numFree--;
        checkReservation();
        return true;
    }

    boolean reservation_cancel() {
        if (numUsed < 1) {
            return false;
        }
        numUsed--;
        numFree++;
        checkReservation();
        return true;
    }

    boolean reservation_updatePrice(int newPrice) {
        if (newPrice < 0) {
            return false;
        }
        this.price = newPrice;
        checkReservation();
        return true;
    }

    int reservation_compare(Reservation aPtr, Reservation bPtr) {
        return aPtr.id - bPtr.id;
    }

    int reservation_hash() {
        return id;
    }
}
