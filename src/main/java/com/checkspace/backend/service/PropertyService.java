package com.checkspace.backend.service;

import com.checkspace.backend.dto.request.CreatePropertyRequest;
import com.checkspace.backend.dto.response.PropertyResponse;
import com.checkspace.backend.model.Property;
import com.checkspace.backend.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.checkspace.backend.repository.PropertyMediaRepository;
import com.checkspace.backend.model.PropertyMedia;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PropertyService {
    private final PropertyMediaRepository propertyMediaRepository;
    private final PropertyRepository propertyRepository;

    @org.springframework.transaction.annotation.Transactional
    public PropertyResponse createProperty(CreatePropertyRequest req) {
        Property property = Property.builder()
                .title(req.getTitle())
                .city(req.getCity())
                .locality(req.getLocality())
                .address(req.getAddress())
                .pincode(req.getPincode())
                .propertyType(req.getPropertyType())
                .bhk(req.getBhk())
                .price(req.getPrice())
                .minPrice(req.getMinPrice())
                .area(req.getArea())
                .furnishing(req.getFurnishing())
                .possessionStatus(req.getPossessionStatus())
                .description(req.getDescription())
                .sellerId(req.getSellerId())
                .status(Property.PropertyStatus.PENDING)
                .visible(false)
                .build();

        Property saved = propertyRepository.save(property);

        if (req.getPhotos() != null) {
            req.getPhotos().forEach(m -> propertyMediaRepository.save(PropertyMedia.builder()
                    .propertyId(saved.getId()).uploadedBy(req.getSellerId())
                    .url(m.getUrl()).publicId(m.getPublicId())
                    .type(PropertyMedia.MediaType.PHOTO).build()));
        }
        if (req.getVideo() != null && req.getVideo().getUrl() != null) {
            propertyMediaRepository.save(PropertyMedia.builder()
                    .propertyId(saved.getId()).uploadedBy(req.getSellerId())
                    .url(req.getVideo().getUrl()).publicId(req.getVideo().getPublicId())
                    .type(PropertyMedia.MediaType.VIDEO).build());
        }
        if (req.getDocuments() != null) {
            req.getDocuments().forEach(m -> propertyMediaRepository.save(PropertyMedia.builder()
                    .propertyId(saved.getId()).uploadedBy(req.getSellerId())
                    .url(m.getUrl()).publicId(m.getPublicId())
                    .type(PropertyMedia.MediaType.DOCUMENT).build()));
        }

        return PropertyResponse.from(saved);
    }

    @org.springframework.cache.annotation.Cacheable(
            value = "propertyListings",
            key = "#city + '-' + #pageable.pageNumber + '-' + #pageable.pageSize"
    )

    public PropertyResponse withdrawProperty(Long propertyId, Long sellerId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found"));

        // Security check — seller can only withdraw their OWN property
        if (!property.getSellerId().equals(sellerId)) {
            throw new RuntimeException("You can only withdraw your own property");
        }

        // Can't withdraw if already sold or token paid by buyer
        if (property.getStatus() == Property.PropertyStatus.SOLD) {
            throw new RuntimeException("Cannot withdraw — property already sold");
        }
        if (property.getStatus() == Property.PropertyStatus.UNDER_NEGOTIATION) {
            throw new RuntimeException(
                    "Cannot withdraw — a buyer has paid token. Contact RootPeace team first.");
        }

        property.setStatus(Property.PropertyStatus.WITHDRAWN);
        property.setVisible(false);
        return PropertyResponse.from(propertyRepository.save(property));
    }

    public List<PropertyResponse> getCachedListings(String city, Pageable pageable) {
        Page<Property> page = (city != null && !city.isEmpty())
                ? propertyRepository.findByVisibleTrueAndStatusAndCity(Property.PropertyStatus.ACTIVE, city, pageable)
                : propertyRepository.findByVisibleTrueAndStatus(Property.PropertyStatus.ACTIVE, pageable);
        return page.map(PropertyResponse::from).getContent();
    }

    public Page<PropertyResponse> getPublicListings(String city, Pageable pageable) {
        List<PropertyResponse> content = getCachedListings(city, pageable);
        long total = (city != null && !city.isEmpty())
                ? propertyRepository.countByVisibleTrueAndStatusAndCity(Property.PropertyStatus.ACTIVE, city)
                : propertyRepository.countByVisibleTrueAndStatus(Property.PropertyStatus.ACTIVE);
        return new org.springframework.data.domain.PageImpl<>(content, pageable, total);
    }

    public PropertyResponse getById(Long id) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Property not found"));

        List<PropertyMedia> media = propertyMediaRepository.findByPropertyId(id);
        List<String> photoUrls = media.stream()
                .filter(m -> m.getType() == PropertyMedia.MediaType.PHOTO)
                .map(PropertyMedia::getUrl).collect(java.util.stream.Collectors.toList());
        String videoUrl = media.stream()
                .filter(m -> m.getType() == PropertyMedia.MediaType.VIDEO)
                .map(PropertyMedia::getUrl).findFirst().orElse(null);

        PropertyResponse response = PropertyResponse.from(property);
        response.setPhotoUrls(photoUrls);
        response.setVideoUrl(videoUrl);
        return response;
    }

    public List<PropertyResponse> getMyListings(Long sellerId) {
        return propertyRepository.findBySellerId(sellerId)
                .stream().map(PropertyResponse::from)
                .collect(Collectors.toList());
    }

    // ADMIN — approve listing, makes it visible
    public PropertyResponse approveListing(Long id) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Property not found"));
        property.setStatus(Property.PropertyStatus.ACTIVE);
        property.setVisible(true);
        return PropertyResponse.from(propertyRepository.save(property));
    }

    public PropertyResponse rejectListing(Long id) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Property not found"));
        property.setStatus(Property.PropertyStatus.REJECTED);
        property.setVisible(false);
        return PropertyResponse.from(propertyRepository.save(property));
    }
}