package com.campusvirtual.controller.student;

import com.campusvirtual.core.AppState;
import com.campusvirtual.model.Submission;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controlador del historial académico del estudiante.
 * Muestra entregas realizadas con calificaciones.
 */
public class HistoryController implements Initializable {
    @FXML private TableView<Submission> historyTable;
    @FXML private TableColumn<Submission, String> colCourse;
    @FXML private TableColumn<Submission, String> colTask;
    @FXML private TableColumn<Submission, String> colDate;
    @FXML private TableColumn<Submission, String> colGrade;
    @FXML private TableColumn<Submission, String> colComment;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        int userId = AppState.getInstance().getUserId();
        List<Submission> submissions = AppState.getInstance().getSubmissionDao().findByStudent(userId);

        colCourse.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCourseName()));
        colTask.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTaskTitle()));
        colDate.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSubmittedAtText()));
        colGrade.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getGradeText()));
        colComment.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getProfessorComment() != null ? c.getValue().getProfessorComment() : "-"));

        historyTable.setItems(FXCollections.observableArrayList(submissions));
    }
}
