package com.bboxxtrack.Service;

import com.bboxxtrack.Model.Zone;
import com.bboxxtrack.Repository.ZoneRepository;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.geojson.GeoJsonReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.List;

@Service
public class ZoneService {

    @Autowired private ZoneRepository repo;

    // JTS factory & GeoJSON reader
    private final GeometryFactory geomFactory = new GeometryFactory();
    private final GeoJsonReader geoJsonReader = new GeoJsonReader(geomFactory);

    public List<Zone> getAllZones() {
        return repo.findAll();
    }

    public Zone saveZone(Zone z) {
        return repo.save(z);
    }

    /**
     * Find the first zone whose polygon contains the given point.
     */
    public Zone findZoneContaining(double lat, double lng) {
        Point p = geomFactory.createPoint(new Coordinate(lng, lat));
        for (Zone z : getAllZones()) {
            try {
                Geometry poly = geoJsonReader.read(z.getPolygonGeoJson());
                if (poly.contains(p)) {
                    return z;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }
}
