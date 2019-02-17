package net.ebdon.trk21;

// @groovy.util.logging.Log4j2('logger')
final class FederationShipMock {
  def energyUsedByLastMove = 0;
  def energyNow = 3000;

  String toString() {
    // "energy: $energyNow, condition: $condition, " +
    // "torpedoes: $numTorpedoes, " +
    "energyUsedByLastMove: $energyUsedByLastMove, energyNow: $energyNow"
  }
}
