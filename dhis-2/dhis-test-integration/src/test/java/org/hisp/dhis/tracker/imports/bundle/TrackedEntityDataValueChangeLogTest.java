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
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.util.List;
import org.hisp.dhis.changelog.ChangeLogType;
import org.hisp.dhis.common.IdentifiableObjectManager;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.program.Event;
import org.hisp.dhis.trackedentity.TrackedEntityDataValueChangeLogQueryParams;
import org.hisp.dhis.trackedentitydatavalue.TrackedEntityDataValueChangeLog;
import org.hisp.dhis.trackedentitydatavalue.TrackedEntityDataValueChangeLogService;
import org.hisp.dhis.tracker.TrackerTest;
import org.hisp.dhis.tracker.imports.TrackerImportParams;
import org.hisp.dhis.tracker.imports.TrackerImportService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Zubair Asghar
 */
public class TrackedEntityDataValueChangeLogTest extends TrackerTest {
  private static final String ORIGINAL_VALUE = "value1";

  private static final String UPDATED_VALUE = "value1-updated";

  private static final String PSI = "D9PbzJY8bJO";

  public static final String DE = "DATAEL00001";

  @Autowired private TrackerImportService trackerImportService;

  @Autowired private IdentifiableObjectManager manager;

  @Autowired private TrackedEntityDataValueChangeLogService dataValueAuditService;

  private DataElement dataElement;

  private Event event;

  @BeforeAll
  void setUp() throws IOException {
    setUpMetadata("tracker/simple_metadata.json");
    injectAdminUser();
  }

  @Test
  void testTrackedEntityDataValueAuditCreate() throws IOException {
    TrackerImportParams params = new TrackerImportParams();
    assertNoErrors(
        trackerImportService.importTracker(
            params, fromJson("tracker/event_and_enrollment_with_data_values.json")));
    assertNoErrors(
        trackerImportService.importTracker(
            params, fromJson("tracker/event_with_data_values_for_update_audit.json")));
    assertNoErrors(
        trackerImportService.importTracker(
            params, fromJson("tracker/event_with_data_values_for_delete_audit.json")));

    dataElement = manager.search(DataElement.class, DE);
    event = manager.search(Event.class, PSI);
    assertNotNull(dataElement);
    assertNotNull(event);

    List<TrackedEntityDataValueChangeLog> createdAudit =
        dataValueAuditService.getTrackedEntityDataValueChangeLogs(
            new TrackedEntityDataValueChangeLogQueryParams()
                .setDataElements(List.of(dataElement))
                .setEvents(List.of(event))
                .setAuditTypes(List.of(ChangeLogType.CREATE)));
    List<TrackedEntityDataValueChangeLog> updatedAudit =
        dataValueAuditService.getTrackedEntityDataValueChangeLogs(
            new TrackedEntityDataValueChangeLogQueryParams()
                .setDataElements(List.of(dataElement))
                .setEvents(List.of(event))
                .setAuditTypes(List.of(ChangeLogType.UPDATE)));
    List<TrackedEntityDataValueChangeLog> deletedAudit =
        dataValueAuditService.getTrackedEntityDataValueChangeLogs(
            new TrackedEntityDataValueChangeLogQueryParams()
                .setDataElements(List.of(dataElement))
                .setEvents(List.of(event))
                .setAuditTypes(List.of(ChangeLogType.DELETE)));

    assertAll(
        () -> assertNotNull(createdAudit),
        () -> assertNotNull(updatedAudit),
        () -> assertNotNull(deletedAudit));
    assertAuditCollection(createdAudit, ChangeLogType.CREATE, ORIGINAL_VALUE);
    assertAuditCollection(updatedAudit, ChangeLogType.UPDATE, ORIGINAL_VALUE);
    assertAuditCollection(deletedAudit, ChangeLogType.DELETE, UPDATED_VALUE);
  }

  private void assertAuditCollection(
      List<TrackedEntityDataValueChangeLog> audits,
      ChangeLogType changeLogType,
      String expectedValue) {
    assertAll(
        () -> assertFalse(audits.isEmpty()),
        () ->
            assertEquals(
                changeLogType,
                audits.get(0).getAuditType(),
                () ->
                    "Expected audit type is "
                        + changeLogType
                        + " but found "
                        + audits.get(0).getAuditType()),
        () ->
            assertEquals(
                audits.get(0).getDataElement().getUid(),
                dataElement.getUid(),
                () ->
                    "Expected dataElement is "
                        + dataElement.getUid()
                        + " but found "
                        + audits.get(0).getDataElement().getUid()),
        () ->
            assertEquals(
                expectedValue,
                audits.get(0).getValue(),
                () ->
                    "Expected value is "
                        + expectedValue
                        + " but found "
                        + audits.get(0).getValue()));
  }
}
