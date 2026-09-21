package com.spipme.app.data.local.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.EntityUpsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.spipme.app.data.local.entity.UtilisateurLocalEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class UtilisateurLocalDao_Impl implements UtilisateurLocalDao {
  private final RoomDatabase __db;

  private final SharedSQLiteStatement __preparedStmtOfToutSupprimer;

  private final EntityUpsertionAdapter<UtilisateurLocalEntity> __upsertionAdapterOfUtilisateurLocalEntity;

  public UtilisateurLocalDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__preparedStmtOfToutSupprimer = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM utilisateur_local";
        return _query;
      }
    };
    this.__upsertionAdapterOfUtilisateurLocalEntity = new EntityUpsertionAdapter<UtilisateurLocalEntity>(new EntityInsertionAdapter<UtilisateurLocalEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT INTO `utilisateur_local` (`id`,`nomUtilisateur`,`email`,`nomComplet`,`hashMotDePasse`,`roleNom`,`secteurPrincipalId`,`secteurPrincipalNom`,`derniereSynchronisation`) VALUES (?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final UtilisateurLocalEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getNomUtilisateur());
        statement.bindString(3, entity.getEmail());
        if (entity.getNomComplet() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getNomComplet());
        }
        statement.bindString(5, entity.getHashMotDePasse());
        if (entity.getRoleNom() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getRoleNom());
        }
        if (entity.getSecteurPrincipalId() == null) {
          statement.bindNull(7);
        } else {
          statement.bindLong(7, entity.getSecteurPrincipalId());
        }
        if (entity.getSecteurPrincipalNom() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getSecteurPrincipalNom());
        }
        statement.bindLong(9, entity.getDerniereSynchronisation());
      }
    }, new EntityDeletionOrUpdateAdapter<UtilisateurLocalEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE `utilisateur_local` SET `id` = ?,`nomUtilisateur` = ?,`email` = ?,`nomComplet` = ?,`hashMotDePasse` = ?,`roleNom` = ?,`secteurPrincipalId` = ?,`secteurPrincipalNom` = ?,`derniereSynchronisation` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final UtilisateurLocalEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getNomUtilisateur());
        statement.bindString(3, entity.getEmail());
        if (entity.getNomComplet() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getNomComplet());
        }
        statement.bindString(5, entity.getHashMotDePasse());
        if (entity.getRoleNom() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getRoleNom());
        }
        if (entity.getSecteurPrincipalId() == null) {
          statement.bindNull(7);
        } else {
          statement.bindLong(7, entity.getSecteurPrincipalId());
        }
        if (entity.getSecteurPrincipalNom() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getSecteurPrincipalNom());
        }
        statement.bindLong(9, entity.getDerniereSynchronisation());
        statement.bindLong(10, entity.getId());
      }
    });
  }

  @Override
  public Object toutSupprimer(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfToutSupprimer.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfToutSupprimer.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object upsert(final UtilisateurLocalEntity utilisateur,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __upsertionAdapterOfUtilisateurLocalEntity.upsert(utilisateur);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object trouverParIdentifiant(final String nomUtilisateur,
      final Continuation<? super UtilisateurLocalEntity> $completion) {
    final String _sql = "SELECT * FROM utilisateur_local WHERE nomUtilisateur = ? OR email = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, nomUtilisateur);
    _argIndex = 2;
    _statement.bindString(_argIndex, nomUtilisateur);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<UtilisateurLocalEntity>() {
      @Override
      @Nullable
      public UtilisateurLocalEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfNomUtilisateur = CursorUtil.getColumnIndexOrThrow(_cursor, "nomUtilisateur");
          final int _cursorIndexOfEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "email");
          final int _cursorIndexOfNomComplet = CursorUtil.getColumnIndexOrThrow(_cursor, "nomComplet");
          final int _cursorIndexOfHashMotDePasse = CursorUtil.getColumnIndexOrThrow(_cursor, "hashMotDePasse");
          final int _cursorIndexOfRoleNom = CursorUtil.getColumnIndexOrThrow(_cursor, "roleNom");
          final int _cursorIndexOfSecteurPrincipalId = CursorUtil.getColumnIndexOrThrow(_cursor, "secteurPrincipalId");
          final int _cursorIndexOfSecteurPrincipalNom = CursorUtil.getColumnIndexOrThrow(_cursor, "secteurPrincipalNom");
          final int _cursorIndexOfDerniereSynchronisation = CursorUtil.getColumnIndexOrThrow(_cursor, "derniereSynchronisation");
          final UtilisateurLocalEntity _result;
          if (_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpNomUtilisateur;
            _tmpNomUtilisateur = _cursor.getString(_cursorIndexOfNomUtilisateur);
            final String _tmpEmail;
            _tmpEmail = _cursor.getString(_cursorIndexOfEmail);
            final String _tmpNomComplet;
            if (_cursor.isNull(_cursorIndexOfNomComplet)) {
              _tmpNomComplet = null;
            } else {
              _tmpNomComplet = _cursor.getString(_cursorIndexOfNomComplet);
            }
            final String _tmpHashMotDePasse;
            _tmpHashMotDePasse = _cursor.getString(_cursorIndexOfHashMotDePasse);
            final String _tmpRoleNom;
            if (_cursor.isNull(_cursorIndexOfRoleNom)) {
              _tmpRoleNom = null;
            } else {
              _tmpRoleNom = _cursor.getString(_cursorIndexOfRoleNom);
            }
            final Integer _tmpSecteurPrincipalId;
            if (_cursor.isNull(_cursorIndexOfSecteurPrincipalId)) {
              _tmpSecteurPrincipalId = null;
            } else {
              _tmpSecteurPrincipalId = _cursor.getInt(_cursorIndexOfSecteurPrincipalId);
            }
            final String _tmpSecteurPrincipalNom;
            if (_cursor.isNull(_cursorIndexOfSecteurPrincipalNom)) {
              _tmpSecteurPrincipalNom = null;
            } else {
              _tmpSecteurPrincipalNom = _cursor.getString(_cursorIndexOfSecteurPrincipalNom);
            }
            final long _tmpDerniereSynchronisation;
            _tmpDerniereSynchronisation = _cursor.getLong(_cursorIndexOfDerniereSynchronisation);
            _result = new UtilisateurLocalEntity(_tmpId,_tmpNomUtilisateur,_tmpEmail,_tmpNomComplet,_tmpHashMotDePasse,_tmpRoleNom,_tmpSecteurPrincipalId,_tmpSecteurPrincipalNom,_tmpDerniereSynchronisation);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
