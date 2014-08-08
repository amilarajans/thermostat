/*
 * Copyright 2012-2014 Red Hat, Inc.
 *
 * This file is part of Thermostat.
 *
 * Thermostat is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2, or (at your
 * option) any later version.
 *
 * Thermostat is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Thermostat; see the file COPYING.  If not see
 * <http://www.gnu.org/licenses/>.
 *
 * Linking this code with other modules is making a combined work
 * based on this code.  Thus, the terms and conditions of the GNU
 * General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this code give
 * you permission to link this code with independent modules to
 * produce an executable, regardless of the license terms of these
 * independent modules, and to copy and distribute the resulting
 * executable under terms of your choice, provided that you also
 * meet, for each linked independent module, the terms and conditions
 * of the license of that module.  An independent module is a module
 * which is not derived from or based on this code.  If you modify
 * this code, you may extend this exception to your version of the
 * library, but you are not obligated to do so.  If you do not wish
 * to do so, delete this exception statement from your version.
 */

package com.redhat.thermostat.notes.client.swing.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.redhat.thermostat.common.Clock;
import com.redhat.thermostat.notes.common.VmNote;
import com.redhat.thermostat.notes.common.VmNoteDAO;
import com.redhat.thermostat.storage.core.VmRef;

public class VmNotesController extends NotesController<VmRef> {

    private VmRef vm;
    private VmNoteDAO dao;
    private List<NoteViewModel> viewModels;

    public VmNotesController(Clock clock, final VmRef vm, VmNoteDAO vmNoteDao, NotesView notesView) {
        super(clock, notesView);
        this.vm = vm;
        this.dao = vmNoteDao;

        viewModels = new ArrayList<>();
    }

    @Override
    protected void addNewNote() {
        long timeStamp = clock.getRealTimeMillis();
        String content = "";
        VmNote note = createNewVmNote(timeStamp, content);

        NoteViewModel model = new NoteViewModel(note.getId(), timeStamp, content);
        viewModels.add(model);
        view.add(model);

        dao.add(note);
    }

    private VmNote createNewVmNote(long timeStamp, String text) {
        VmNote vmNote = new VmNote();
        vmNote.setAgentId(vm.getHostRef().getAgentId());
        vmNote.setVmId(vm.getVmId());
        vmNote.setId(UUID.randomUUID().toString());
        vmNote.setTimeStamp(timeStamp);
        vmNote.setContent(text);
        return vmNote;
    }

    @Override
    protected void updateNotesInView() {
        List<VmNote> vmNotes = dao.getFor(vm);

        // TODO only apply diff of notes to reduce UI glitches/changes
        viewModels.clear();
        view.clearAll();

        for (int i = 0; i < vmNotes.size(); i++) {
            VmNote vmNote = vmNotes.get(i);
            NoteViewModel viewModel = new NoteViewModel(vmNote.getId(), vmNote.getTimeStamp(), vmNote.getContent());
            viewModels.add(viewModel);
            view.add(viewModel);
        }
    }

    @Override
    protected void updateNotesInStorage() {
        // TODO check if we need any synchronization

        long timeStamp = clock.getRealTimeMillis();

        for (NoteViewModel viewModel : viewModels) {
            String oldContent = viewModel.text;
            String newContent = view.getContent(viewModel.tag);
            if (oldContent.equals(newContent)) {
                continue;
            }

            VmNote toUpdate = dao.getById(vm, viewModel.tag);
            toUpdate.setTimeStamp(timeStamp);
            toUpdate.setContent(newContent);

            dao.update(toUpdate);
        }
    }

    @Override
    protected void deleteNote(String noteId) {
        dao.removeById(vm, noteId);

        updateNotesInView();
    }

}