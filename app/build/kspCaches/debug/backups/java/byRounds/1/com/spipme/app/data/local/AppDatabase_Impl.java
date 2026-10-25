package com.spipme.app.data.local;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import com.spipme.app.data.local.dao.CacheDao;
import com.spipme.app.data.local.dao.CacheDao_Impl;
import com.spipme.app.data.local.dao.CorrespondanceDao;
import com.spipme.app.data.local.dao.CorrespondanceDao_Impl;
import com.spipme.app.data.local.dao.OperationDao;
import com.spipme.app.data.local.dao.OperationDao_Impl;
import com.spipme.app.data.local.dao.UtilisateurLocalDao;
import com.spipme.app.data.local.dao.UtilisateurLocalDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile UtilisateurLocalDao _utilisateurLocalDao;

  private volatile OperationDao _operationDao;

  private volatile CorrespondanceDao _correspondanceDao;

  private volatile CacheDao _cacheDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(2) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `utilisateur_local` (`id` INTEGER NOT NULL, `nomUtilisateur` TEXT NOT NULL, `email` TEXT NOT NULL, `nomComplet` TEXT, `hashMotDePasse` TEXT NOT NULL, `roleNom` TEXT, `secteurPrincipalId` INTEGER, `secteurPrincipalNom` TEXT, `derniereSynchronisation` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `operation_en_attente` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `idempotencyKey` TEXT NOT NULL, `type` TEXT NOT NULL, `entityType` TEXT NOT NULL, `entityId` TEXT NOT NULL, `methode` TEXT NOT NULL, `chemin` TEXT NOT NULL, `payloadJson` TEXT, `versionBase` INTEGER, `refLocaleCreee` TEXT, `creeLe` INTEGER NOT NULL, `tentatives` INTEGER NOT NULL, `derniereTentativeLe` INTEGER, `prochaineTentativeLe` INTEGER NOT NULL, `statut` TEXT NOT NULL, `messageErreur` TEXT, `conflitJson` TEXT, `utilisateurId` INTEGER NOT NULL)");
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_operation_en_attente_idempotencyKey` ON `operation_en_attente` (`idempotencyKey`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_operation_en_attente_statut` ON `operation_en_attente` (`statut`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_operation_en_attente_utilisateurId` ON `operation_en_attente` (`utilisateurId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `correspondance_id` (`referenceLocale` TEXT NOT NULL, `idServeur` TEXT NOT NULL, PRIMARY KEY(`referenceLocale`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `cache_reponse` (`cle` TEXT NOT NULL, `json` TEXT NOT NULL, `majLe` INTEGER NOT NULL, PRIMARY KEY(`cle`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '70120a02c25c5fed48705128584217c9')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `utilisateur_local`");
        db.execSQL("DROP TABLE IF EXISTS `operation_en_attente`");
        db.execSQL("DROP TABLE IF EXISTS `correspondance_id`");
        db.execSQL("DROP TABLE IF EXISTS `cache_reponse`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsUtilisateurLocal = new HashMap<String, TableInfo.Column>(9);
        _columnsUtilisateurLocal.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUtilisateurLocal.put("nomUtilisateur", new TableInfo.Column("nomUtilisateur", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUtilisateurLocal.put("email", new TableInfo.Column("email", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUtilisateurLocal.put("nomComplet", new TableInfo.Column("nomComplet", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUtilisateurLocal.put("hashMotDePasse", new TableInfo.Column("hashMotDePasse", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUtilisateurLocal.put("roleNom", new TableInfo.Column("roleNom", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUtilisateurLocal.put("secteurPrincipalId", new TableInfo.Column("secteurPrincipalId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUtilisateurLocal.put("secteurPrincipalNom", new TableInfo.Column("secteurPrincipalNom", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUtilisateurLocal.put("derniereSynchronisation", new TableInfo.Column("derniereSynchronisation", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysUtilisateurLocal = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesUtilisateurLocal = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoUtilisateurLocal = new TableInfo("utilisateur_local", _columnsUtilisateurLocal, _foreignKeysUtilisateurLocal, _indicesUtilisateurLocal);
        final TableInfo _existingUtilisateurLocal = TableInfo.read(db, "utilisateur_local");
        if (!_infoUtilisateurLocal.equals(_existingUtilisateurLocal)) {
          return new RoomOpenHelper.ValidationResult(false, "utilisateur_local(com.spipme.app.data.local.entity.UtilisateurLocalEntity).\n"
                  + " Expected:\n" + _infoUtilisateurLocal + "\n"
                  + " Found:\n" + _existingUtilisateurLocal);
        }
        final HashMap<String, TableInfo.Column> _columnsOperationEnAttente = new HashMap<String, TableInfo.Column>(18);
        _columnsOperationEnAttente.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOperationEnAttente.put("idempotencyKey", new TableInfo.Column("idempotencyKey", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOperationEnAttente.put("type", new TableInfo.Column("type", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOperationEnAttente.put("entityType", new TableInfo.Column("entityType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOperationEnAttente.put("entityId", new TableInfo.Column("entityId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOperationEnAttente.put("methode", new TableInfo.Column("methode", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOperationEnAttente.put("chemin", new TableInfo.Column("chemin", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOperationEnAttente.put("payloadJson", new TableInfo.Column("payloadJson", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOperationEnAttente.put("versionBase", new TableInfo.Column("versionBase", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOperationEnAttente.put("refLocaleCreee", new TableInfo.Column("refLocaleCreee", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOperationEnAttente.put("creeLe", new TableInfo.Column("creeLe", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOperationEnAttente.put("tentatives", new TableInfo.Column("tentatives", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOperationEnAttente.put("derniereTentativeLe", new TableInfo.Column("derniereTentativeLe", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOperationEnAttente.put("prochaineTentativeLe", new TableInfo.Column("prochaineTentativeLe", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOperationEnAttente.put("statut", new TableInfo.Column("statut", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOperationEnAttente.put("messageErreur", new TableInfo.Column("messageErreur", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOperationEnAttente.put("conflitJson", new TableInfo.Column("conflitJson", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOperationEnAttente.put("utilisateurId", new TableInfo.Column("utilisateurId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysOperationEnAttente = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesOperationEnAttente = new HashSet<TableInfo.Index>(3);
        _indicesOperationEnAttente.add(new TableInfo.Index("index_operation_en_attente_idempotencyKey", true, Arrays.asList("idempotencyKey"), Arrays.asList("ASC")));
        _indicesOperationEnAttente.add(new TableInfo.Index("index_operation_en_attente_statut", false, Arrays.asList("statut"), Arrays.asList("ASC")));
        _indicesOperationEnAttente.add(new TableInfo.Index("index_operation_en_attente_utilisateurId", false, Arrays.asList("utilisateurId"), Arrays.asList("ASC")));
        final TableInfo _infoOperationEnAttente = new TableInfo("operation_en_attente", _columnsOperationEnAttente, _foreignKeysOperationEnAttente, _indicesOperationEnAttente);
        final TableInfo _existingOperationEnAttente = TableInfo.read(db, "operation_en_attente");
        if (!_infoOperationEnAttente.equals(_existingOperationEnAttente)) {
          return new RoomOpenHelper.ValidationResult(false, "operation_en_attente(com.spipme.app.data.local.entity.OperationEnAttenteEntity).\n"
                  + " Expected:\n" + _infoOperationEnAttente + "\n"
                  + " Found:\n" + _existingOperationEnAttente);
        }
        final HashMap<String, TableInfo.Column> _columnsCorrespondanceId = new HashMap<String, TableInfo.Column>(2);
        _columnsCorrespondanceId.put("referenceLocale", new TableInfo.Column("referenceLocale", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCorrespondanceId.put("idServeur", new TableInfo.Column("idServeur", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCorrespondanceId = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesCorrespondanceId = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoCorrespondanceId = new TableInfo("correspondance_id", _columnsCorrespondanceId, _foreignKeysCorrespondanceId, _indicesCorrespondanceId);
        final TableInfo _existingCorrespondanceId = TableInfo.read(db, "correspondance_id");
        if (!_infoCorrespondanceId.equals(_existingCorrespondanceId)) {
          return new RoomOpenHelper.ValidationResult(false, "correspondance_id(com.spipme.app.data.local.entity.CorrespondanceIdEntity).\n"
                  + " Expected:\n" + _infoCorrespondanceId + "\n"
                  + " Found:\n" + _existingCorrespondanceId);
        }
        final HashMap<String, TableInfo.Column> _columnsCacheReponse = new HashMap<String, TableInfo.Column>(3);
        _columnsCacheReponse.put("cle", new TableInfo.Column("cle", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCacheReponse.put("json", new TableInfo.Column("json", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCacheReponse.put("majLe", new TableInfo.Column("majLe", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCacheReponse = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesCacheReponse = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoCacheReponse = new TableInfo("cache_reponse", _columnsCacheReponse, _foreignKeysCacheReponse, _indicesCacheReponse);
        final TableInfo _existingCacheReponse = TableInfo.read(db, "cache_reponse");
        if (!_infoCacheReponse.equals(_existingCacheReponse)) {
          return new RoomOpenHelper.ValidationResult(false, "cache_reponse(com.spipme.app.data.local.entity.CacheReponseEntity).\n"
                  + " Expected:\n" + _infoCacheReponse + "\n"
                  + " Found:\n" + _existingCacheReponse);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "70120a02c25c5fed48705128584217c9", "bde8403b78ac6a81c19f8887bdbc4ec8");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "utilisateur_local","operation_en_attente","correspondance_id","cache_reponse");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `utilisateur_local`");
      _db.execSQL("DELETE FROM `operation_en_attente`");
      _db.execSQL("DELETE FROM `correspondance_id`");
      _db.execSQL("DELETE FROM `cache_reponse`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(UtilisateurLocalDao.class, UtilisateurLocalDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(OperationDao.class, OperationDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(CorrespondanceDao.class, CorrespondanceDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(CacheDao.class, CacheDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public UtilisateurLocalDao utilisateurLocalDao() {
    if (_utilisateurLocalDao != null) {
      return _utilisateurLocalDao;
    } else {
      synchronized(this) {
        if(_utilisateurLocalDao == null) {
          _utilisateurLocalDao = new UtilisateurLocalDao_Impl(this);
        }
        return _utilisateurLocalDao;
      }
    }
  }

  @Override
  public OperationDao operationDao() {
    if (_operationDao != null) {
      return _operationDao;
    } else {
      synchronized(this) {
        if(_operationDao == null) {
          _operationDao = new OperationDao_Impl(this);
        }
        return _operationDao;
      }
    }
  }

  @Override
  public CorrespondanceDao correspondanceDao() {
    if (_correspondanceDao != null) {
      return _correspondanceDao;
    } else {
      synchronized(this) {
        if(_correspondanceDao == null) {
          _correspondanceDao = new CorrespondanceDao_Impl(this);
        }
        return _correspondanceDao;
      }
    }
  }

  @Override
  public CacheDao cacheDao() {
    if (_cacheDao != null) {
      return _cacheDao;
    } else {
      synchronized(this) {
        if(_cacheDao == null) {
          _cacheDao = new CacheDao_Impl(this);
        }
        return _cacheDao;
      }
    }
  }
}
