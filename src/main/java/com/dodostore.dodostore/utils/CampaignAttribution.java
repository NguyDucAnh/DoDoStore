package com.dodostore.dodostore.utils;
import com.dodostore.dodostore.entity.*;
public final class CampaignAttribution {
    private CampaignAttribution() {}
    public static Long appliedCampaign(ProductEntity product, Long appliedDiscount) {
        FlashSaleCampaign campaign = product.getFlashSaleCampaign();
        long regular = product.getDiscount() == null ? 0 : product.getDiscount();
        if (campaign == null || !campaign.isEnabled() || !product.isFlashSaleActive()
            || product.getFlashSaleDiscount() == null || product.getFlashSaleDiscount() <= regular
            || !java.util.Objects.equals(appliedDiscount, product.getFlashSaleDiscount())) return null;
        return campaign.getId();
    }
}
