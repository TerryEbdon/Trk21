package net.ebdon.trk21;

import static GameSpace.*
import static Quadrant.*

@groovy.util.logging.Log4j2('logger')
final class RepositionerDiagonalTest extends RepositionerTestBase {

  // if ( notYetImplemented() ) return
  final void testTransitDiagonal() {
    logger.info 'testTransitDiagonal'
    def courseIncrements = [1,1]
    logger.info "Transit with offsets of $courseIncrements"
    transit( *courseIncrements )
    logger.info "Transit with offsets of $courseIncrements -- OK"

    logger.info 'testTransitDiagonal -- OK'
  }

  @Override
  protected ShipVector getTransitShipVector( final course ) {
    shipWarpDir 12, course
  }

  @Override
  protected def getExpectedTransitCoords( final int stepNum, final expectedRowOffset, final expectedColOffset ) {
    [8,8]
  }

  final void transit( final expectedRowOffset, final expectedColOffset ) {
    logger.info "Transit with expected offsets: $expectedRowOffset, $expectedColOffset"
    assert expectedRowOffset == expectedColOffset

    final cornerToCornerDistance = Math.sqrt( 2 * maxCoord**2 )
    final stepSize = Math.sin( Math.toRadians( 45 ) )
    final int maxSteps = Math.round( cornerToCornerDistance / stepSize )
    logger.info "Calling transitSteps for $maxSteps steps"
    transitSteps expectedRowOffset, expectedColOffset, maxSteps

    logger.info "Transit with expected offsets: " +
      "$expectedRowOffset, $expectedColOffset -- OK"
  }
}
