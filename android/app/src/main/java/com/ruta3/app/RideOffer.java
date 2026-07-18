package com.ruta3.app;

public class RideOffer {
    public final String platform;
    public final int amount;
    public final double pickupMin;
    public final double tripMin;
    public final double pickupKm;
    public final double tripKm;
    public final double totalMin;
    public final double totalKm;
    public final double perKm;
    public final double perHour;
    public final boolean meets;

    public RideOffer(
            String platform,
            int amount,
            double pickupMin,
            double tripMin,
            double pickupKm,
            double tripKm,
            int minPerKm,
            int minPerHour
    ) {
        this.platform = platform;
        this.amount = amount;
        this.pickupMin = pickupMin;
        this.tripMin = tripMin;
        this.pickupKm = pickupKm;
        this.tripKm = tripKm;
        this.totalMin = pickupMin + tripMin;
        this.totalKm = pickupKm + tripKm;
        this.perKm = totalKm > 0 ? amount / totalKm : 0;
        this.perHour = totalMin > 0 ? (amount / totalMin) * 60 : 0;
        this.meets = perKm >= minPerKm && perHour >= minPerHour;
    }

    public boolean isComplete() {
        return amount > 0 && totalMin > 0 && totalKm > 0;
    }
}
