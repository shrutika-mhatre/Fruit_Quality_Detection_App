package com.example.appledisease;

public class DiseaseInfo {
    public final String displayName;
    public final String description;

    private DiseaseInfo(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public static DiseaseInfo getInfo(String label) {
        switch (label) {
            case "blotch_apple":
                return new DiseaseInfo(
                        "🟠 Apple Blotch",
                        "Apple Blotch is a fungal infection commonly associated with Diplocarpon coronariae, causing irregular dark brown or black patches on the apple skin. The disease affects the fruit's appearance and lowers its market value. In mild cases, the unaffected portion may still be used for juice or processing after removing damaged parts, but severe infection makes the fruit less suitable for consumption. It can spread quickly in orchards during rainy seasons and impact overall fruit production."
                );
            case "normal_apple":
                return new DiseaseInfo(
                        "✅ Normal Apple",
                        "A normal apple is healthy, fresh, and free from any visible disease or damage. It has smooth skin, bright natural color, and firm texture without dark spots or soft areas. Normal apples are completely safe to eat and are rich in vitamins, fiber, and antioxidants. They have high market value and are suitable for direct consumption, juice preparation, salads, and other food products. There is no harm in consuming a normal apple if it is properly washed before eating."
                );
            case "rot_apple":
                return new DiseaseInfo(
                        "🟣 Apple Rot",
                        "Apple Rot is a serious fungal disease often caused by fungi such as Botryosphaeria obtusa, which leads to soft, mushy texture, bad odor, and dark rotten areas on the fruit. Rotting apples are unsafe to eat because they may contain harmful microorganisms that can cause stomach problems. This condition spreads rapidly to nearby fruits and results in major losses for farmers. Rotten apples should always be discarded to prevent contamination and health risks."
                );
            case "scab_apple":
                return new DiseaseInfo(
                        "🟡 Apple Scab",
                        "Apple Scab is a fungal disease caused by Venturia inaequalis, which creates dark, rough, and cracked spots on the apple's surface. While mild scab infections may not affect the inside of the fruit and the healthy portion can sometimes be eaten after removing damaged areas, severe infection reduces quality, taste, and market value. It mainly affects appearance and can spread to other fruits in humid conditions, making it harmful for farmers and commercial sale."
                );
            default:
                return new DiseaseInfo("❓ Unknown",
                        "Detection label not recognized. Please retake the image.");
        }
    }
}