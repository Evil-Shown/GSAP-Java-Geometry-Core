package com.company.gsap.engine.cutout.custom;

import com.company.gsap.engine.cutout.custom.impl.CustomCutout_100;
import com.company.gsap.engine.cutout.custom.impl.CustomCutout_101;
import com.company.gsap.engine.cutout.custom.impl.CustomCutout_102;
import com.company.gsap.engine.cutout.custom.impl.CustomCutout_103;
import com.company.gsap.engine.cutout.custom.impl.CustomCutout_104;
import com.company.gsap.engine.cutout.custom.impl.CustomCutout_105;
import com.company.gsap.engine.cutout.custom.impl.CustomCutout_106;
import com.company.gsap.engine.cutout.custom.impl.CustomCutout_107;
import com.company.gsap.engine.cutout.custom.impl.CustomCutout_108;
import com.company.gsap.engine.cutout.custom.impl.CustomCutout_109;
import com.company.gsap.engine.cutout.custom.impl.CustomCutout_110;
import com.company.gsap.engine.cutout.custom.impl.CustomCutout_111;

import java.util.List;

public final class CustomCutoutFactory {
    private CustomCutoutFactory() {
    }

    public static CustomCutout create(CustomCutoutRequest request, List<double[]> polygonPoints) {
        CustomCutoutGeometryEngine engine = new CustomCutoutGeometryEngine(polygonPoints);
        String cutoutNo = request.cutoutNo() == null ? "" : request.cutoutNo().trim();
        return switch (cutoutNo) {
            case "100" -> new CustomCutout_100(request, engine);
            case "101" -> new CustomCutout_101(request, engine);
            case "102" -> new CustomCutout_102(request, engine);
            case "103" -> new CustomCutout_103(request, engine);
            case "104" -> new CustomCutout_104(request, engine);
            case "105" -> new CustomCutout_105(request, engine);
            case "106" -> new CustomCutout_106(request, engine);
            case "107" -> new CustomCutout_107(request, engine);
            case "108" -> new CustomCutout_108(request, engine);
            case "109" -> new CustomCutout_109(request, engine);
            case "110" -> new CustomCutout_110(request, engine);
            case "111" -> new CustomCutout_111(request, engine);
            default -> throw new IllegalArgumentException("Unsupported custom cutoutNo: " + cutoutNo);
        };
    }
}
