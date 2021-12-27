package org.rigel5.plugin;

import java.io.File;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.rigel5.db.AbstractGenListe;

/**
 * Goal which touches a timestamp file.
 */
@Mojo(name = "listexml", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class RigelListeGeneratorMojo
   extends AbstractMojo
{
  @Parameter(defaultValue = "${project.build.directory}", property = "outputDirXml", required = true)
  private File outputDirXml;

  @Parameter(defaultValue = "${project.build.directory}", property = "outputDirSql", required = true)
  private File outputDirSql;

  @Parameter(defaultValue = "${project.basedir}/src/main/torque-schema", property = "schemaDir", required = true)
  private File schemaDir;

  @Parameter(defaultValue = "mysql", property = "adapter", required = true)
  private String adapter;

  @Override
  public void execute()
     throws MojoExecutionException
  {
    getLog().info("Generazione liste XML in " + outputDirXml.getAbsolutePath());
    getLog().info("Generazione viste SQL in " + outputDirSql.getAbsolutePath());

    outputDirSql.mkdirs();
    outputDirXml.mkdirs();

    scanDir();
  }

  private void scanDir()
     throws MojoExecutionException
  {
    getLog().info("Scan dir " + schemaDir.getAbsolutePath());

    if(!schemaDir.isDirectory())
    {
      getLog().info("Directory inesistente.");
      return;
    }

    File[] files = schemaDir.listFiles();

    if(files == null || files.length == 0)
    {
      getLog().info("Directory vuota.");
      return;
    }

    for(File fi : files)
    {
      if(fi.getName().toLowerCase().endsWith("-schema.xml"))
      {
        try
        {
          genFile(fi);
        }
        catch(Exception ex)
        {
          throw new MojoExecutionException("Errore nel parsing di " + fi.getAbsolutePath(), ex);
        }
      }
    }
  }

  private void genFile(File fi)
     throws Exception
  {
    getLog().info("Generating for " + fi.getAbsolutePath());

    GenListe gl = new GenListe();
    gl.xmlFile = fi.getAbsolutePath();
    gl.adapter = adapter;

    File fxml = new File(outputDirXml, fi.getName().toLowerCase().replace("-schema.xml", "-modelli.xml"));
    if(fxml.exists() && fxml.length() > 0 && fxml.lastModified() >= fi.lastModified())
      getLog().info(fxml.getAbsolutePath() + " è aggiornato.");
    else
      gl.ouputFile = fxml.getAbsolutePath();

    File fsql = new File(outputDirSql, fi.getName().toLowerCase().replace("-schema.xml", "-viste.sql"));
    if(fsql.exists() && fsql.length() > 0 && fsql.lastModified() >= fi.lastModified())
      getLog().info(fsql.getAbsolutePath() + " è aggiornato.");
    else
      gl.sqloutput = fsql.getAbsolutePath();

    if(gl.ouputFile != null || gl.sqloutput != null)
      gl.run();
  }

  /**
   * Generatore automatico di liste XML.
   * @author Nicola De Nisco
   */
  public class GenListe extends AbstractGenListe
  {
    public String[] arNoFieldsListe =
    {
      "STATO_REC", "ID_USER", "ID_UCREA", "ULT_MODIF", "CREAZIONE", "UUID"
    };
    public String[] arFieldsDescrizione =
    {
      "DESCRIZIONE", "NOME", "COGNOME"
    };
    public String[] toRemove =
    {
      "INF.STP_", "INF.IN_", "INF.OUT_", "INF.RUN_", "ANA.", "INF.", "GEN.", "STP.", "RIS.", "APP.", "SAT."
    };
    //
    public static final String DATE_FORMATTER = "it.flower.flowercore.utils.format.DateTimeFormat";
    public static final String STYLE_TECH_FIELD = "cell_form_tech";

    public GenListe()
    {
      xmlFile = "application-schema.xml";
      compatTorque4 = true;
      ouputFile = sqloutput = null;
    }

    @Override
    public String[] getArNoFieldsListe()
    {
      return arNoFieldsListe;
    }

    @Override
    public String[] getArFieldsDescrizione()
    {
      return arFieldsDescrizione;
    }

    @Override
    public String[] getArtoRemove()
    {
      return toRemove;
    }

    @Override
    public String getStyleTechField()
    {
      return STYLE_TECH_FIELD;
    }

    @Override
    public String getDateFormatter()
    {
      return DATE_FORMATTER;
    }
  }
}
