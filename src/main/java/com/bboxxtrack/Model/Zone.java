package com.bboxxtrack.Model;

import jakarta.persistence.*;

@Entity
@Table(name="zone")
public class Zone {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    // store the polygon geometry as GeoJSON (GeoJSON spec: coordinates in [lng,lat])
    @Column(columnDefinition = "TEXT")
    private String polygonGeoJson;

    // getters & setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPolygonGeoJson() { return polygonGeoJson; }
    public void setPolygonGeoJson(String polygonGeoJson) {
        this.polygonGeoJson = polygonGeoJson;
    }
}
