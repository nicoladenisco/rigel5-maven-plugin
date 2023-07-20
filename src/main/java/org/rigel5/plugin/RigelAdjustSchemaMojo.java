package org.rigel5.plugin;

import java.io.File;
import java.util.*;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.rigel5.db.AbstractAdjustSchema;

/**
 * Goal which touches a timestamp file.
 */
@Mojo(name = "adjschema", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class RigelAdjustSchemaMojo
   extends AbstractMojo
{
  private final HashSet<String> skipName = new HashSet<>();

  @Parameter(defaultValue = "${project.build.directory}", property = "outputDirXml", required = true)
  private File outputDirXml;

  @Parameter(defaultValue = "${project.build.directory}", property = "outputDirSql", required = true)
  private File outputDirSql;

  @Parameter(defaultValue = "${project.basedir}/src/main/torque-schema", property = "schemaDir", required = true)
  private File schemaDir;

  @Parameter(defaultValue = "mysql", property = "adapter", required = true)
  private String adapter;

  @Parameter(defaultValue = "false", property = "inplace", required = false)
  private boolean inplace;

  @Parameter(defaultValue = "false", property = "pointVsUndScore", required = false)
  private boolean pointVsUndScore;

  @Parameter(defaultValue = "false", property = "forcePrimary", required = false)
  private boolean forcePrimary;

  @Parameter(defaultValue = "false", property = "forceForeign", required = false)
  private boolean forceForeign;

  @Parameter(defaultValue = "false", property = "forceIDUndScore", required = false)
  private boolean forceIDUndScore;

  @Parameter(defaultValue = "false", property = "forceTableJavaName", required = false)
  private boolean forceTableJavaName;

  @Parameter(defaultValue = "false", property = "useSchema", required = false)
  private boolean useSchema;

  @Parameter(defaultValue = "false", property = "forcePrimaryIDtoNative", required = false)
  private boolean forcePrimaryIDtoNative;

  @Parameter(defaultValue = "false", property = "addStatoRec", required = false)
  private boolean addStatoRec = false;

  @Parameter(defaultValue = "4", property = "minColStatoRec", required = false)
  private int minColStatoRec = 4;

  @Parameter(defaultValue = "null", property = "omBaseClass", required = false)
  private String omBaseClass = null;

  @Parameter(defaultValue = "null", property = "omBasePeer", required = false)
  private String omBasePeer = null;

  @Override
  public void execute()
     throws MojoExecutionException
  {
    getLog().info("Generazione schema XML in " + outputDirXml.getAbsolutePath());
    getLog().info("Generazione info   SQL in " + outputDirSql.getAbsolutePath());
    AdjustSchema.configuraLog4j2();

    outputDirSql.mkdirs();
    outputDirXml.mkdirs();

    skipName.add("id-table-schema.xml");
    skipName.add("torque-security-schema.xml");

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
        if(skipName.contains(fi.getName()))
        {
          getLog().info("Ignorato il file " + fi.getName());
          continue;
        }

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

    AdjustSchema as = new AdjustSchema();

    as.pointVsUndScore = pointVsUndScore;
    as.forcePrimary = forcePrimary;
    as.forceForeign = forceForeign;
    as.forceIDUndScore = forceIDUndScore;
    as.forceTableJavaName = forceTableJavaName;
    as.useSchema = useSchema;
    as.forcePrimaryIDtoNative = forcePrimaryIDtoNative;

    if("postgresql".equals(adapter))
      as.usePostgresql = true;

    as.addStatoRec = addStatoRec;
    as.minColStatoRec = minColStatoRec;

    if(omBaseClass != null && !"null".equals(omBaseClass))
      as.omBaseClass = omBaseClass;

    if(omBasePeer != null && !"null".equals(omBasePeer))
      as.omBasePeer = omBasePeer;

    as.xmlFile = fi.getAbsolutePath();

    if(outputDirSql != null)
    {
      File f = new File(outputDirSql, fi.getName().replace(".xml", ".txt"));
      as.infoFile = f.getAbsolutePath();
      getLog().info("Info file in " + as.infoFile);
    }

    if(inplace)
    {
      // verifica il timestamp del file con il suo backup se esiste
      File backup = new File(fi.getParentFile(), fi.getName() + "-backup");
      if(!backup.exists() || fi.lastModified() > backup.lastModified())
      {
        as.run();

        // genera il backup del file corrente
        backup.delete();
        fi.renameTo(backup);

        // salva il nuovo documento XML al post del precedente
        as.ouputFile = fi.getAbsolutePath();
        as.print(as.doc);
        com.google.common.io.Files.touch(backup);
      }
      else
        getLog().info("Skip " + fi.getAbsolutePath() + ": update.");
    }
    else
    {
      as.run();

      File f = new File(outputDirXml, fi.getName());
      if(f.exists())
        f = new File(outputDirXml, fi.getName() + "-new");

      as.ouputFile = f.getAbsolutePath();
      as.print(as.doc);
    }
  }

  public class AdjustSchema extends AbstractAdjustSchema
  {
  }
}
