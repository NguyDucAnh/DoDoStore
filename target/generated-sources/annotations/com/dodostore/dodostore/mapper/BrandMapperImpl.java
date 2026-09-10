package com.dodostore.dodostore.mapper;

import com.dodostore.dodostore.dto.request.BrandRequest;
import com.dodostore.dodostore.dto.response.BrandResponse;
import com.dodostore.dodostore.entity.BrandEntity;
import com.dodostore.dodostore.entity.ProductEntity;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-09-10T20:52:16+0700",
    comments = "version: 1.6.0, compiler: javac, environment: Java 21.0.4 (Oracle Corporation)"
)
@Component
public class BrandMapperImpl implements BrandMapper {

    @Override
    public BrandResponse toBrandResponse(BrandEntity brandEntity) {
        if ( brandEntity == null ) {
            return null;
        }

        BrandResponse.BrandResponseBuilder brandResponse = BrandResponse.builder();

        brandResponse.id( brandEntity.getId() );
        brandResponse.name( brandEntity.getName() );
        brandResponse.image( brandEntity.getImage() );
        List<ProductEntity> list = brandEntity.getProducts();
        if ( list != null ) {
            brandResponse.products( new ArrayList<ProductEntity>( list ) );
        }

        return brandResponse.build();
    }

    @Override
    public BrandEntity toEntity(BrandRequest brandRequest) {
        if ( brandRequest == null ) {
            return null;
        }

        BrandEntity.BrandEntityBuilder brandEntity = BrandEntity.builder();

        brandEntity.name( brandRequest.getName() );
        brandEntity.image( brandRequest.getImage() );

        return brandEntity.build();
    }
}
