/*
 * Copyright (c) 2004-2022, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.hisp.dhis.tracker.imports.bundle;

import static org.hisp.dhis.tracker.Assertions.assertNoErrors;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.List;
import org.hisp.dhis.program.Event;
import org.hisp.dhis.tracker.TrackerTest;
import org.hisp.dhis.tracker.imports.TrackerImportParams;
import org.hisp.dhis.tracker.imports.TrackerImportService;
import org.hisp.dhis.tracker.imports.TrackerImportStrategy;
import org.hisp.dhis.tracker.imports.domain.TrackerObjects;
import org.hisp.dhis.tracker.imports.report.ImportReport;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Morten Olav Hansen <mortenoh@gmail.com>
 */
class TrackerEventBundleServiceTest extends TrackerTest {
  @Autowired private TrackerImportService trackerImportService;

  @BeforeAll
  void setUp() throws IOException {
    setUpMetadata("tracker/event_metadata.json");
    injectAdminUser();
  }

  @Test
  void testCreateSingleEventData() throws IOException {
    TrackerObjects trackerObjects = fromJson("tracker/event_events_and_enrollment.json");
    assertEquals(8, trackerObjects.getEvents().size());
    ImportReport importReport =
        trackerImportService.importTracker(new TrackerImportParams(), trackerObjects);
    assertNoErrors(importReport);

    List<Event> events = manager.getAll(Event.class);
    assertEquals(8, events.size());
  }

  @Test
  void testUpdateSingleEventData() throws IOException {
    TrackerObjects trackerObjects = fromJson("tracker/event_events_and_enrollment.json");
    TrackerImportParams trackerImportParams = new TrackerImportParams();
    trackerImportParams.setImportStrategy(TrackerImportStrategy.CREATE_AND_UPDATE);
    ImportReport importReport =
        trackerImportService.importTracker(trackerImportParams, trackerObjects);
    assertNoErrors(importReport);
    assertEquals(8, manager.getAll(Event.class).size());

    importReport = trackerImportService.importTracker(trackerImportParams, trackerObjects);
    assertNoErrors(importReport);

    assertEquals(8, manager.getAll(Event.class).size());
  }
}
