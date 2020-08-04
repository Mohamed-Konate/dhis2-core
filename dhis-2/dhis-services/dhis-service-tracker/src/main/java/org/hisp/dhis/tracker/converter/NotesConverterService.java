package org.hisp.dhis.tracker.converter;

/*
 * Copyright (c) 2004-2020, University of Oslo
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

import java.util.List;
import java.util.stream.Collectors;

import org.hisp.dhis.trackedentitycomment.TrackedEntityComment;
import org.hisp.dhis.tracker.domain.Note;
import org.hisp.dhis.tracker.preheat.TrackerPreheat;
import org.hisp.dhis.util.DateUtils;
import org.springframework.stereotype.Service;

/**
 * @author Luciano Fiandesio
 */
@Service
public class NotesConverterService implements TrackerConverterService<Note, TrackedEntityComment>
{
    @Override
    public Note to( TrackedEntityComment trackedEntityComment )
    {
        Note note = new Note();
        note.setNote( trackedEntityComment.getUid() );
        note.setValue( trackedEntityComment.getCommentText() );
        note.setStoredAt( DateUtils.getIso8601NoTz( trackedEntityComment.getCreated() ) );
        note.setStoredBy( trackedEntityComment.getCreator() );
        return note;
    }

    @Override
    public List<Note> to( List<TrackedEntityComment> trackedEntityComments )
    {
        return trackedEntityComments.stream().map( this::to ).collect( Collectors.toList() );
    }

    @Override
    public TrackedEntityComment from( Note note )
    {
        TrackedEntityComment comment = new TrackedEntityComment();
        comment.setAutoFields();
        comment.setCommentText( note.getValue() );
        comment.setUid( note.getNote() );

        // FIXME: what about the storedBy and lastUpdatedBy -> currently they are set to null
        return comment;
    }

    @Override
    public TrackedEntityComment from( TrackerPreheat preheat, Note note )
    {
        return from( note );
    }

    @Override
    public List<TrackedEntityComment> from( List<Note> notes )
    {
        return notes.stream().map( this::from ).collect( Collectors.toList() );
    }
}
