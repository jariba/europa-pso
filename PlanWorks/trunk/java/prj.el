;;; $Id: prj.el,v 1.11 2004-07-08 21:33:20 taylor Exp $
;;;
;;; JAVA_HOME ANT_HOME JDE_HOME & PLANWORKS_HOME must be set properly
;;;
;;; M-x byte-compile-file
;;;
(jde-project-file-version "1.0")
(jde-set-variables
 '(jde-ant-args (concat "-emacs -Djavac=" (getenv "JAVA_HOME")
                        "/bin/javac -Djar=" (getenv "JAVA_HOME") "/bin/jar compile"))
 '(jde-ant-buildfile (concat (getenv "PLANWORKS_HOME") "/build.xml"))
 '(jde-ant-complete-target nil)
 '(jde-ant-home (getenv "ANT_HOME"))
 '(jde-ant-read-target nil)
 '(jde-bug-jdk-directory (getenv "JAVA_HOME"))
 '(jde-bug-jpda-directory (getenv "JAVA_HOME"))
 '(jde-bug-vm-includes-jpda-p t)


; '(jde-checkstyle-classpath (list (concat (getenv "JDE_HOME") "/java/lib")))
; '(jde-checkstyle-option-allow-paren-padding t)
; '(jde-checkstyle-option-allow-protected t)
; '(jde-checkstyle-option-ignore-imports nil)
; '(jde-checkstyle-option-javadoc-scope (quote ("public")))
; '(jde-checkstyle-option-maxlinelen 100)
; '(jde-checkstyle-option-wrap-operator nil)


 '(jde-compile-option-classpath
   (list (concat (getenv "JAVA_HOME") "/jre/lib/rt.jar")
         (concat (getenv "PLANWORKS_HOME") "/java/classes/")
         (concat (getenv "PLANWORKS_HOME") "/java/lib/JGo/")
         (concat (getenv "PLANWORKS_HOME") "/java/lib/JGo/JGo.jar")
         (concat (getenv "PLANWORKS_HOME") "/java/lib/JGo/JGoLayout.jar")
         (concat (getenv "PLANWORKS_HOME") "/java/lib/JGo/Classier.jar")
         (concat (getenv "PLANWORKS_HOME") "/java/lib/JFCUnit/junit.jar")
         (concat (getenv "PLANWORKS_HOME") "/java/lib/JFCUnit/jfcunit.jar")
         (concat (getenv "PLANWORKS_HOME") "/java/lib/JFCUnit/jakarta-regexp.jar")
         ))
 ;; either -g or -deprecation, not both
 '(jde-compile-option-command-line-args (quote ("-g")))
;; '(jde-compile-option-command-line-args (quote ("-deprecation")))
 '(jde-compile-option-directory (concat (getenv "PLANWORKS_HOME") "/java/classes"))
 '(jde-compile-option-sourcepath (list (concat (getenv "PLANWORKS_HOME") "/java/src")
                                       (concat (getenv "JAVA_HOME") "/src")))
 '(jde-compiler (list "javac" (concat (getenv "JAVA_HOME") "/bin/javac")))
 '(jde-complete-function (quote jde-complete-minibuf))
 '(jde-complete-insert-method-signature t)
 '(jde-complete-signature-display (quote ("Buffer")))
 '(jde-complete-unique-method-names nil)
 '(jde-complete-use-menu nil)
 '(jde-db-classic-mode-vm nil)
 '(jde-db-initial-step-p nil)
 '(jde-db-option-application-args (quote ("linux" "true" "false" "false" "null" "null" "null")))
 '(jde-db-option-classpath nil)
 '(jde-db-option-vm-args (list "-verbose:gc" "-Xms32m" "-Xmx192m"
                               (concat "-Dplanviz.root=" (getenv "PLANWORKS_HOME"))
                               ))
 '(jde-db-set-initial-breakpoint nil)
 '(jde-db-source-directories (list (concat (getenv "PLANWORKS_HOME") "/java/src")
                                   (concat (getenv "JAVA_HOME") "/src")))
 '(jde-db-startup-commands nil)
 '(jde-debugger (quote ("JDEbug")))
 '(jde-gen-cflow-enable nil)
 '(jde-global-classpath
   (list (concat (getenv "JAVA_HOME") "/jre/lib/rt.jar")
         (concat (getenv "JDE_HOME") "/java/lib/bsh.jar")
         (concat (getenv "JDE_HOME") "/java/lib/jde.jar")
         (concat (getenv "PLANWORKS_HOME") "/java/classes/")
         (concat (getenv "PLANWORKS_HOME") "/java/lib/JGo/")
         (concat (getenv "PLANWORKS_HOME") "/java/lib/JGo/JGo.jar")
         (concat (getenv "PLANWORKS_HOME") "/java/lib/JGo/JGoLayout.jar")
         (concat (getenv "PLANWORKS_HOME") "/java/lib/JGo/Classier.jar")
         (concat (getenv "PLANWORKS_HOME") "/java/lib/JFCUnit/junit.jar")
         (concat (getenv "PLANWORKS_HOME") "/java/lib/JFCUnit/jfcunit.jar")
         (concat (getenv "PLANWORKS_HOME") "/java/lib/JFCUnit/jakarta-regexp.jar")
         ))
 ;; Do you prefer to have java.io.* imports or separate import for each 
 ;; used class - now it is set for importing classes separately
 '(jde-import-auto-collapse-imports nil)
 ;; Nice feature sorting imports.
 '(jde-import-auto-sort t)
 '(jde-javadoc-author-tag-template "\"* @author <a href=\\\"mailto:\" user-mail-address
 \"\\\">\" user-full-name \"</a>\" \" NASA Ames Research Center - Code IC\"")
 '(jde-javadoc-describe-class-template "\"* \" (jde-javadoc-code name) \" - \"")
 '(jde-javadoc-describe-constructor-template "\"* \" (jde-javadoc-code name) \" - constructor \"")
 '(jde-javadoc-describe-field-template "\"* \" (jde-javadoc-field-type modifiers)
 \" \" (jde-javadoc-code name)")
 '(jde-javadoc-describe-method-template "\"* \" (jde-javadoc-code name) ")
 '(jde-javadoc-param-tag-template "\"* @param \" name \" - \" 
 (jde-javadoc-code type) \" - \"")
 '(jde-javadoc-return-tag-template "\"* @return - \" 
 (jde-javadoc-code type) \" - \"")
 '(jde-javadoc-version-tag-template "\"* @version 0.0\"")
 '(jde-jdk-registry (list (cons "1.4.1_02" (getenv "JAVA_HOME"))))
 '(jde-run-application-class "gov.nasa.arc.planworks.PlanWorks")
 '(jde-run-executable-args nil)
 '(jde-run-java-vm (concat (getenv "JAVA_HOME") "/bin/java"))
 '(jde-run-option-application-args (quote ("linux" "true" "false" "false" "null" "null" "null")))
 '(jde-run-option-debug (quote (nil "Attach" nil)))
 '(jde-run-option-vm-args
   (list "-verbose:gc" "-Xms32m" "-Xmx192m"
         (concat "-Dplanviz.root=" (getenv "PLANWORKS_HOME"))
         "-Duser=wtaylor" ))
 '(jde-run-working-directory (concat (getenv "PLANWORKS_HOME") "/java/src"))
 '(jde-sourcepath
   (list (concat (getenv "PLANWORKS_HOME") "/java/src/gov/nasa/arc/planworks")
         (concat (getenv "PLANWORKS_HOME") "/java/src/gov/nasa/arc/planworks/db")
         (concat (getenv "PLANWORKS_HOME") "/java/src/gov/nasa/arc/planworks/db/impl")
         (concat (getenv "PLANWORKS_HOME") "/java/src/gov/nasa/arc/planworks/europa")
         (concat (getenv "PLANWORKS_HOME") "/java/src/gov/nasa/arc/planworks/viz")
         (concat (getenv "PLANWORKS_HOME") "/java/src/gov/nasa/arc/planworks/viz/nodes")
         (concat (getenv "PLANWORKS_HOME") "/java/src/gov/nasa/arc/planworks/viz/views")
         (concat (getenv "PLANWORKS_HOME") "/java/src/gov/nasa/arc/planworks/db/util")))
)
