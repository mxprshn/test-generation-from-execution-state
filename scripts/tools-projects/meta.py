"""Writes ~/newcases/tools/meta.json: signature, group, hidden_paths and expected tool applicability per candidate.

Rules (from refined/baselines.md and the tools' papers):
 - Pankti: public, non-static instance method of a project class; receiver and arguments serialisable by XStream.
 - RICK: Pankti's rule plus at least one call, inside the target, to a method of a field of the receiver whose
   type is a project type (a mockable dependency).
 - MicroTestCarver: records only on JDK 11; all four projects here build and run their tests on JDK 11.
"""
import json, os

PB = "pdfbox/src/test/java/org/apache/pdfbox/"
GH = "core/src/test/java/com/graphhopper/"
PC = "src/test/java/org/springframework/samples/petclinic/"
LAB = "pricing-service/src/test/java/pl/altkom/asc/lab/micronaut/poc/pricing/"

def T(pankti=True, rick=False, mtc=True, note=""):
    return {"pankti": pankti, "rick": rick, "microtestcarver": mtc, **({"note": note} if note else {})}

M = {
 "pdfbox-catalog-get-viewer-preferences": dict(signature="() -> org.apache.pdfbox.pdmodel.interactive.viewerpreferences.PDViewerPreferences",
    hidden=[PB + "pdmodel/TestPDDocumentCatalog.java"], tools=T(rick=True, note="root.getDictionaryObject: COSDictionary field")),
 "pdfbox-xref-trailer-resolver-get-xref-type": dict(signature="() -> org.apache.pdfbox.pdfparser.XrefTrailerResolver$XRefType",
    hidden=[], tools=T(note="no call to a field method: RICK not applicable")),
 "pdfbox-cos-parser-get-encryption": dict(signature="() -> org.apache.pdfbox.pdmodel.encryption.PDEncryption",
    hidden=[], tools=T(note="receiver holds an open RandomAccess stream: XStream serialisation risk")),
 "pdfbox-font-descriptor-is-symbolic": dict(signature="() -> boolean", hidden=[], tools=T(note="calls own isFlagBitOn/getFlags, not a field")),
 "pdfbox-font-descriptor-get-flags": dict(signature="() -> int", hidden=[], tools=T(rick=True, note="dic.getInt on the first call (flags cache)")),
 "pdfbox-font-descriptor-is-fixed-pitch": dict(signature="() -> boolean", hidden=[], tools=T()),
 "pdfbox-cid-system-info-get-ordering": dict(signature="() -> java.lang.String", hidden=[], tools=T(rick=True, note="dictionary.getNameAsString")),
 "pdfbox-cid-font-get-cid-system-info": dict(signature="() -> org.apache.pdfbox.pdmodel.font.PDCIDSystemInfo", hidden=[], tools=T(rick=True, note="dict.getDictionaryObject")),
 "pdfbox-icc-based-get-default-decode": dict(signature="(int) -> float[]", hidden=[PB + "pdmodel/graphics/color/PDICCBasedTest.java"],
    tools=T(note="receiver holds java.awt.color.ICC_ColorSpace: XStream serialisation risk")),
 "pdfbox-image-xobject-get-color-key-mask": dict(signature="() -> org.apache.pdfbox.cos.COSArray",
    hidden=[PB + "pdmodel/graphics/image/PDImageXObjectTest.java"], tools=T(note="calls own getCOSObject(): RICK not applicable")),
 "pdfbox-image-xobject-get-interpolate": dict(signature="() -> boolean",
    hidden=[PB + "pdmodel/graphics/image/PDImageXObjectTest.java"], tools=T(note="calls own getCOSObject(): RICK not applicable")),
 "pdfbox-ext-graphics-state-get-soft-mask": dict(signature="() -> org.apache.pdfbox.pdmodel.graphics.state.PDSoftMask", hidden=[], tools=T(rick=True, note="dict.containsKey / dict.getDictionaryObject")),
 "pdfbox-resources-get-ext-gstate": dict(signature="(org.apache.pdfbox.cos.COSName) -> org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState",
    hidden=[], tools=T(rick=True, note="cache.getExtGState: ResourceCache field")),
 "pdfbox-default-resource-cache-get-font": dict(signature="(org.apache.pdfbox.cos.COSObject) -> org.apache.pdfbox.pdmodel.font.PDFont",
    hidden=[], tools=T(note="field is a java.util.Map (JDK type): mockable only if RICK mocks JDK types")),
 "gh-string-index-load-existing": dict(signature="() -> boolean", hidden=[GH + "search/StringIndexTest.java"],
    tools=T(rick=True, note="keys/vals DataAccess field calls (as in RICK's own test)")),
 "gh-storable-properties-load-existing": dict(signature="() -> boolean", hidden=[GH + "storage/StorablePropertiesTest.java"],
    tools=T(rick=True, note="da.loadExisting/getCapacity/getBytes (as in RICK's own test)")),
 "gh-line-int-index-load-existing": dict(signature="() -> boolean", hidden=[], tools=T(rick=True, note="dataAccess.loadExisting/getHeader")),
 "gh-base-graph-nodes-and-edges-load-existing": dict(signature="() -> boolean", hidden=[], tools=T(rick=True, note="nodes/edges DataAccess calls")),
 "gh-graphhopper-storage-load-existing": dict(signature="() -> boolean",
    hidden=[GH + "storage/GraphHopperStorageTest.java", GH + "storage/GraphHopperStorageWithTurnCostsTest.java",
            GH + "storage/GraphHopperStorageLMTest.java", GH + "storage/GraphStorageViaMMapTest.java"],
    tools=T(rick=True, note="properties/encodingManager/baseGraph field calls")),
 "gh-virtual-edge-iterator-state-get-reverse": dict(signature="(com.graphhopper.routing.ev.BooleanEncodedValue) -> boolean", hidden=[],
    tools=T(note="the mocked call is on the argument, not on a field: RICK not applicable")),
 "gh-douglas-peucker-set-max-distance": dict(signature="(double) -> com.graphhopper.util.DouglasPeucker", hidden=[GH + "util/DouglasPeuckerTest.java"],
    tools=T(rick=True, note="calc.calcNormalizedDist: DistanceCalc field (as in RICK's own test)")),
 "gh-int-encoded-value-impl-init": dict(signature="(com.graphhopper.routing.ev.EncodedValue$InitializerConfig) -> int",
    hidden=[GH + "routing/ev/IntEncodedValueImplTest.java"], tools=T(pankti=False, note="final method; the call is on the argument: Pankti instruments it, RICK not applicable")),
 "petclinic-pet-type-formatter-parse": dict(signature="(java.lang.String, java.util.Locale) -> org.springframework.samples.petclinic.owner.PetType",
    hidden=[PC + "owner/PetTypeFormatterTests.java"], tools=T(rick=True, note="owners.findPetTypes: repository field (Mockito mock in the scenario)")),
 "petclinic-owner-get-pet": dict(signature="(java.lang.Integer) -> org.springframework.samples.petclinic.owner.Pet", hidden=[], tools=T(note="calls only own getPets()")),
 "petclinic-owner-add-visit": dict(signature="(java.lang.Integer, org.springframework.samples.petclinic.owner.Visit) -> org.springframework.samples.petclinic.owner.Owner",
    hidden=[], tools=T(note="calls own getPet and Pet.addVisit on a local")),
 "petclinic-owner-get-pet-by-name": dict(signature="(java.lang.String, boolean) -> org.springframework.samples.petclinic.owner.Pet", hidden=[], tools=T()),
 "petclinic-vets-get-vet-list": dict(signature="() -> java.util.List<org.springframework.samples.petclinic.vet.Vet>",
    hidden=[], tools=T()),
 "petclinic-named-entity-to-string": dict(signature="() -> java.lang.String", hidden=[], tools=T()),
 "petclinic-base-entity-is-new": dict(signature="() -> boolean", hidden=[], tools=T()),
 "petclinic-pet-validator-validate": dict(signature="(java.lang.Object, org.springframework.validation.Errors) -> void",
    hidden=[], tools=T(note="errors.rejectValue is on an argument")),
 "lab-calculate-price-handler-handle": dict(signature="(pl.altkom.asc.lab.micronaut.poc.pricing.service.api.v1.commands.calculateprice.CalculatePriceCommand) -> pl.altkom.asc.lab.micronaut.poc.pricing.service.api.v1.commands.calculateprice.CalculatePriceResult",
    hidden=[], tools=T(rick=True, note="tariffs.getByCode: repository field; receiver is a Micronaut bean proxy")),
 "lab-percent-markup-rule-apply": dict(signature="(pl.altkom.asc.lab.micronaut.poc.pricing.domain.Calculation) -> pl.altkom.asc.lab.micronaut.poc.pricing.domain.Calculation",
    hidden=[LAB + "domain/TariffTest.java"], tools=T(note="receiver holds a JPA entity (Tariff)")),
 "lab-tariff-calculate-price": dict(signature="(pl.altkom.asc.lab.micronaut.poc.pricing.domain.Calculation) -> pl.altkom.asc.lab.micronaut.poc.pricing.domain.Calculation",
    hidden=[LAB + "domain/TariffTest.java"], tools=T(note="JPA entity with lazy collections: XStream risk")),
}
LAB_PREBUILD = [["mvn", "-q", "-o", "-DskipTests", "install", "-f", m] for m in ("command-bus-api", "command-bus", "pricing-service-api")]
out = {}
for k, v in M.items():
    out[k] = {"signature": v["signature"], "hidden_paths": v["hidden"], "tools_expected": v["tools"],
              "group": "indirect", "prebuild": LAB_PREBUILD if k.startswith("lab-") else None}
json.dump(out, open(os.path.expanduser("~/newcases/tools/meta.json"), "w"), indent=1)
print(len(out))
