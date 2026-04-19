package com.campusvirtual.core;

import com.campusvirtual.dao.*;
import com.campusvirtual.model.enums.AccountType;


/**
 * Singleton que mantiene el estado global de la aplicación.
 * Gestiona la sesión del usuario actual y proporciona acceso centralizado
 * a ViewFactory y los DAOs.
 */
public class AppState {
    private static AppState instance;
    private final ViewFactory viewFactory;

    // DAOs centralizados
    private final UserDao userDao;
    private final CourseDao courseDao;
    private final UnitDao unitDao;
    private final TaskDao taskDao;
    private final FileDao fileDao;
    private final SubmissionDao submissionDao;
    private final LoginAttemptDao loginAttemptDao;
    private final AuditLogDao auditLogDao;

    // Datos del usuario logueado
    private int userId;
    private String userFirstName;
    private String userLastName;
    private String userEmail;
    private AccountType userRole;

    private AppState() {
        this.viewFactory = new ViewFactory();
        this.userDao = new UserDao();
        this.courseDao = new CourseDao();
        this.unitDao = new UnitDao();
        this.taskDao = new TaskDao();
        this.fileDao = new FileDao();
        this.submissionDao = new SubmissionDao();
        this.loginAttemptDao = new LoginAttemptDao();
        this.auditLogDao = new AuditLogDao();
    }

    /** Acceso thread-safe a la instancia única. */
    public static synchronized AppState getInstance() {
        if (instance == null) instance = new AppState();
        return instance;
    }

    // ── Sesión del usuario ────────────────────────────────────

    /**
     * Establece los datos del usuario tras un login exitoso.
     */
    public void setLoggedUser(int id, String firstName, String lastName,
                              String email, AccountType role) {
        this.userId = id;
        this.userFirstName = firstName;
        this.userLastName = lastName;
        this.userEmail = email;
        this.userRole = role;
    }

    /**
     * Limpia la sesión del usuario y reinicia el estado.
     * DEBE llamarse en cada logout para evitar datos residuales.
     */
    public void clearSession() {
        this.userId = 0;
        this.userFirstName = null;
        this.userLastName = null;
        this.userEmail = null;
        this.userRole = null;
    }

    // ── Getters de sesión ────────────────────────────────────

    public int getUserId() { return userId; }
    public String getUserFirstName() { return userFirstName; }
    public String getUserLastName() { return userLastName; }
    public String getUserFullName() { return userFirstName + " " + userLastName; }
    public String getUserEmail() { return userEmail; }
    public AccountType getUserRole() { return userRole; }

    // ── Getters de servicios ─────────────────────────────────

    public ViewFactory getViewFactory() { return viewFactory; }
    public UserDao getUserDao() { return userDao; }
    public CourseDao getCourseDao() { return courseDao; }
    public UnitDao getUnitDao() { return unitDao; }
    public TaskDao getTaskDao() { return taskDao; }
    public FileDao getFileDao() { return fileDao; }
    public SubmissionDao getSubmissionDao() { return submissionDao; }
    public LoginAttemptDao getLoginAttemptDao() { return loginAttemptDao; }
    public AuditLogDao getAuditLogDao() { return auditLogDao; }
}
