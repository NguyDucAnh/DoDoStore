package com.dodostore.dodostore.service.impl;
import com.dodostore.dodostore.repository.ProductRepository;
import com.dodostore.dodostore.enums.StatusEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.regex.Pattern;
@Service @RequiredArgsConstructor
public class ChatProductCardService {
    private final ProductRepository products;
    public record Card(Long id,String name,String image,String url,Long price,Long salePrice,Long discount) {}
    @Transactional(readOnly=true)
    public List<Card> fromReply(String reply) {
        Set<Long> ids=new LinkedHashSet<>();
        var matcher=Pattern.compile("/home/product/(\\d+)").matcher(reply==null?"":reply);
        while(matcher.find() && ids.size()<10) {
            try { ids.add(Long.parseLong(matcher.group(1))); } catch(NumberFormatException ignored) {}
        }
        if(ids.isEmpty()) return List.of();
        Map<Long,Card> cards=new HashMap<>();
        for(var p:products.findAllById(ids)) {
            if(p.getStatus()!=StatusEnum.Active || p.getPrice()==null) continue;
            String image=p.getImages()==null || p.getImages().isEmpty()?null:p.getImages().get(0).getImageLink();
            long discount=p.getEffectiveDiscount();
            cards.put(p.getId(),new Card(p.getId(),p.getName(),image,"/home/product/"+p.getId(),p.getPrice(),p.getPrice()*(100-discount)/100,discount));
        }
        return ids.stream().map(cards::get).filter(Objects::nonNull).toList();
    }
}
