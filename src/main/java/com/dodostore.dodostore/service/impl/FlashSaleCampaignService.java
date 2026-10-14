package com.dodostore.dodostore.service.impl;
import com.dodostore.dodostore.entity.*;
import com.dodostore.dodostore.enums.*;
import com.dodostore.dodostore.repository.*;
import com.dodostore.dodostore.dto.request.FlashSaleCampaignRequest;
import com.dodostore.dodostore.utils.FlashSale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;
@Service @RequiredArgsConstructor
public class FlashSaleCampaignService {
    private final FlashSaleCampaignRepository campaigns;
    private final ProductRepository products;
    private final OrderDetailRepository orderDetails;
    @Transactional(readOnly=true)
    public List<FlashSaleCampaign> list() {
        List<FlashSaleCampaign> result=campaigns.findAllByOrderByCreatedAtDescIdDesc();
        Map<Long, Long> sold = new HashMap<>();
        orderDetails.sumCampaignSales(List.of(StatusOrderEnum.Dang_Giao, StatusOrderEnum.Da_Giao))
            .forEach(s -> sold.put(s.getCampaignId(), s.getSoldQuantity()));
        result.forEach(c -> { c.setProductCount(products.countByFlashSaleCampaignId(c.getId())); c.setSoldQuantity(sold.getOrDefault(c.getId(), 0L)); });
        return result;
    }
    @Transactional(readOnly=true)
    public List<ProductEntity> availableProducts() {
        return products.findByStatus(StatusEnum.Active).stream().sorted(Comparator.comparing(ProductEntity::getId)).toList();
    }
    @Transactional(readOnly=true)
    public FlashSaleCampaignRequest edit(Long id) {
        FlashSaleCampaign c=campaigns.findById(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chiến dịch."));
        FlashSaleCampaignRequest r=new FlashSaleCampaignRequest();r.setId(c.getId());r.setVersion(c.getVersion());
        r.setName(c.getName());r.setScope(c.getScope());r.setDiscount(c.getDiscount());r.setStartsAt(c.getStartsAt());r.setEndsAt(c.getEndsAt());
        r.setProductIds(products.findByFlashSaleCampaignIdOrderByIdAsc(id).stream().filter(p -> p.getStatus()==StatusEnum.Active).map(ProductEntity::getId).toList());
        return r;
    }
    @Transactional
    public int save(FlashSaleCampaignRequest r) {
        LocalDateTime now=LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        if (r.getName()==null || r.getName().isBlank() || r.getName().trim().length()>120 || r.getScope()==null)
            throw new IllegalArgumentException("Nhập tên và phạm vi chiến dịch hợp lệ.");
        if (r.getDiscount()==null || r.getDiscount()<1) throw new IllegalArgumentException("Mức giảm từ 1 đến 99%.");
        FlashSale.validate(r.getDiscount(),r.getStartsAt(),r.getEndsAt());
        if (!r.getEndsAt().isAfter(now)) throw new IllegalArgumentException("Thời gian kết thúc phải ở tương lai.");
        FlashSaleCampaign c;
        if (r.getId()==null) { c=new FlashSaleCampaign();c.setCreatedAt(now); }
        else {
            c=campaigns.lockById(r.getId()).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chiến dịch."));
            if (!Objects.equals(c.getVersion(),r.getVersion())) throw new IllegalArgumentException("Chiến dịch đã được thay đổi. Hãy tải lại trang trước khi sửa.");
        }
        // Lock products in a consistent order, shared with checkout and individual product edits.
        List<ProductEntity> candidates=products.lockCampaignCandidates(StatusEnum.Active,r.getId());
        Set<Long> selected=new HashSet<>(r.getProductIds()==null ? List.of() : r.getProductIds());
        List<ProductEntity> targets=candidates.stream().filter(p -> p.getStatus()==StatusEnum.Active)
                .filter(p -> r.getScope()==FlashSaleScope.ALL || selected.contains(p.getId())).toList();
        if (targets.isEmpty()) throw new IllegalArgumentException("Vui lòng chọn ít nhất một sản phẩm đang bán.");
        if (r.getScope()==FlashSaleScope.SELECTED && !targets.stream().map(ProductEntity::getId).collect(Collectors.toSet()).equals(selected))
            throw new IllegalArgumentException("Một sản phẩm đã ngừng bán hoặc không còn tồn tại. Vui lòng tải lại danh sách.");
        Set<Long> targetIds=targets.stream().map(ProductEntity::getId).collect(Collectors.toSet());
        for (ProductEntity p:candidates) {
            if (r.getId()!=null && p.getFlashSaleCampaign()!=null && r.getId().equals(p.getFlashSaleCampaign().getId()) && !targetIds.contains(p.getId())) clear(p);
        }
        c.setUpdatedAt(now);c.setName(r.getName().trim());c.setScope(r.getScope());c.setDiscount(r.getDiscount());c.setStartsAt(r.getStartsAt());c.setEndsAt(r.getEndsAt());
        campaigns.save(c);
        for (ProductEntity p:targets) {
            p.setFlashSaleCampaign(c);p.setFlashSaleDiscount(c.isEnabled() ? c.getDiscount() : 0L);p.setFlashSaleStart(c.getStartsAt());p.setFlashSaleEnd(c.getEndsAt());p.setUpdateDate(new Date());
        }
        products.saveAll(candidates);
        return targets.size();
    }
    @Transactional
    public void stop(Long id) {
        FlashSaleCampaign c=campaigns.lockById(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chiến dịch."));
        if (!c.isEnabled()) return;
        List<ProductEntity> owned=products.lockByCampaign(id);
        owned.forEach(p -> { p.setFlashSaleDiscount(0L); p.setUpdateDate(new Date()); });products.saveAll(owned);c.setEnabled(false);c.setUpdatedAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));campaigns.save(c);
    }
    @Transactional
    public int resume(Long id) {
        FlashSaleCampaign c=campaigns.lockById(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chiến dịch."));
        if (c.isEnabled()) throw new IllegalArgumentException("Chiến dịch đã được bật. Vui lòng tải lại trang.");
        if (!c.isResumable()) throw new IllegalArgumentException("Chiến dịch đã hết hạn. Hãy sửa thời gian kết thúc trước khi tiếp tục.");
        List<ProductEntity> owned=products.lockByCampaign(id).stream().filter(p -> p.getStatus()==StatusEnum.Active).toList();
        if (owned.isEmpty()) throw new IllegalArgumentException("Không còn sản phẩm đang bán thuộc chiến dịch. Hãy sửa và chọn lại sản phẩm.");
        c.setEnabled(true);c.setUpdatedAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
        for (ProductEntity p:owned) {
            p.setFlashSaleDiscount(c.getDiscount());p.setFlashSaleStart(c.getStartsAt());p.setFlashSaleEnd(c.getEndsAt());p.setUpdateDate(new Date());
        }
        products.saveAll(owned);campaigns.save(c);return owned.size();
    }
    private void clear(ProductEntity p) {
        p.setFlashSaleCampaign(null);p.setFlashSaleDiscount(0L);p.setFlashSaleStart(null);p.setFlashSaleEnd(null);p.setUpdateDate(new Date());
    }
}
