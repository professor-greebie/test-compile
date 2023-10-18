package com.greebiestudios.test_data_generator.data

class ARCGISDataCaseClasses {

    case class ARCGISData(
        features: Seq[ARCGISFeature],

    )

    case class ARCGISFeature(
        attributes: ARCGISAttributes,
        geometry: ARCGISGeometry,
    )

    trait ARCGISAttributes
    trait ARCGISGeometry

    case class ARCGISGeometryDefault(
        x: Double,
        y: Double,
    ) extends ARCGISGeometry

    case class ARCGISAttributesPropertyWaterloo (
        OBJECTID: Int,
        PROPERTY_UNIT_ID: Int,
        CIVIC_NO: Int, 
        STREET: String,
        UNIT: String,
        OWNERNAME: String,
        GlobalID: String,
    ) extends ARCGISAttributes
  
}
