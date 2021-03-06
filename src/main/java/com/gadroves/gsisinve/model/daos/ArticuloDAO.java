package com.gadroves.gsisinve.model.daos;

import com.gadroves.gsisinve.model.DataBase;
import com.gadroves.gsisinve.model.beans.Articulo;
import com.gadroves.gsisinve.model.beans.Bodega;
import com.gadroves.gsisinve.model.beans.Inventario;
import com.gadroves.gsisinve.model.daos.DAOInterfaces.*;
import com.gadroves.gsisinve.utils.Dataformat;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by Casa on 14/02/2015.
 */
public class ArticuloDAO {
    private static ArticuloDAO ourInstance = new ArticuloDAO();
    public static ResultSetProcessor<Articulo> processor = (rs, rw) -> {
        Articulo art = new Articulo(rs.getNString("id"), rs.getNString("desc"), rs.getDouble("cost"), rs.getDouble("util"), true);
        art.setEsGrabado(rs.getByte("grav") == 1);
        return art;
    };

    public static ArticuloDAO getInstance() {
        return ourInstance;
    }

    private ArticuloDAO() {
    }

    public IntermediateArticuloSelect select() {
        return new IntermediateArticuloSelect();
    }
    public IntermdiateArticuuloUpdate update() {
        return new IntermdiateArticuuloUpdate();
    }
    public IntermediateArticuloInsert insert() {
        return new IntermediateArticuloInsert();
    }
    public IntermediateArticuloDelete delete() {
        return new IntermediateArticuloDelete();
    }

    public class IntermediateArticuloSelect implements IntermediateSelect<Articulo, String> {
        Bodega bodegaOrigen = null;
        boolean buscarEnTodasLasBodegas = true;

        /**
         * Getter for Bodega
         *
         * @return the Selected Bodega, null if Bodegas hasn't being set
         */
        public Bodega getBodegaOrigen() {
            return bodegaOrigen;
        }

        /**
         * Sets the Bodega to restrict the search
         *
         * @param origen The Bodega
         * @return the modified object
         */
        public IntermediateArticuloSelect setBodegaOrigen(Bodega origen) {
            this.bodegaOrigen = origen;
            this.buscarEnTodasLasBodegas=false;
            return this;
        }

        /**
         * Set if search must include all Bodegas
         * @param s True to enable
         * @return Modified Object
         */
        public IntermediateSelect setBuscarTodaslasBodegas(boolean s) {
            this.buscarEnTodasLasBodegas = s;
            return this;
        }

        @Override
        final public List<Articulo> all() {
            ArrayList<Articulo> articulos = new ArrayList<>();
            try (Connection c = DataBase.getInstance().getConnection()) {
                Statement stm = c.createStatement();
                if (buscarEnTodasLasBodegas)
                    retrieveAll(stm, articulos);
                else {
                    //TODO Mejorar Estos llamados
                    retrieveAll(stm, articulos);
                    if (bodegaOrigen == null) throw new NullPointerException("Bodega de Origen No ha sido establecida");
                    List<String> codigos = InventarioDAO.getInstance().select().where(b -> b.getCodigo_bodega().equals(bodegaOrigen.getCodigo())).stream().map(Inventario::getCodigo_articulo).collect(Collectors.toList());
                    return articulos.stream().filter(a -> codigos.contains(a.getCodigo())).collect(Collectors.toList());
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return articulos;
        }

        private void retrieveAll(Statement stm, List<Articulo> ls) {
            try {
                ResultSet rs = stm.executeQuery("SELECT * FROM TB_Articulo");
                int i = 0;
                while (rs.next()) {
                    ls.add(processor.process(rs, i++));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        @Override
        final public Articulo byID(String pk) {
            Articulo a = null;
            try (Connection c = DataBase.getInstance().getConnection()) {
                Statement stm = c.createStatement();
                ResultSet rs = stm.executeQuery("SELECT * FROM TB_Articulo WHERE TB_Articulo.id = '" + pk+"'");
                while (rs.next())
                    a = processor.process(rs, 0);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return a;
        }

        @SafeVarargs
        @Override
        final public List<Articulo> where(Predicate<Articulo>... pl) {
            if (pl.length == 0) return all();
            Predicate<Articulo> ini = pl[0];
            for (int i = 1; i < pl.length; i++) ini = ini.and(pl[i]);
            return all().stream().filter(ini).collect(Collectors.toList());
        }

        /**
         * Retrieves all the Articulos that match the idList
         *
         * @param idList A list containing the desired Articulos
         * @return A new List with the Articulos found
         */
        final public List<Articulo> fromIdList(List<String> idList) {
            Articulo a = null;
            List<Articulo> articulos = new ArrayList<>();
            try (Connection c = DataBase.getInstance().getConnection()) {
                Statement stm = c.createStatement();
                StringBuilder sb = new StringBuilder();
                idList.stream().forEach(s -> sb.append("'").append(s).append("',"));
                sb.deleteCharAt(sb.lastIndexOf(","));
                String sql = "";
                if (buscarEnTodasLasBodegas)
                    sql = "SELECT * FROM TB_Articulo art where art.id IN (" + sb.toString() + ")";
                else
                    sql = "SELECT * FROM TB_Articulo art where art.id IN (SELECT inv.code_art FROM TB_Inventario inv WHERE inv.code_art IN (" + sb.toString() + ") AND inv.code_bod = '" + bodegaOrigen.getCodigo() + "')";
                ResultSet rs = stm.executeQuery(sql);
                while (rs.next()) {
                    a = processor.process(rs, 0);
                    if (a != null) articulos.add(a);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return articulos;
        }

        /**
         * Retrieves all Articulos for a Bodega Set
         *
         * @param bodegas the list of Bodegas to Search for
         * @return a List containing all the Objects for that Bodega
         */
        final public List<Articulo> AllInBodegaList(List<Bodega> bodegas) {
            List<Articulo> articulos = new ArrayList<>();
            Articulo a;
            try (Connection c = DataBase.getInstance().getConnection()) {
                Statement stm = c.createStatement();
                StringBuilder sb = new StringBuilder();
                bodegas.stream().forEach(s -> sb.append("'").append(s.getCodigo()).append("',"));
                sb.deleteCharAt(sb.lastIndexOf(","));
                String sql = "";
                sql = "SELECT * FROM TB_Articulo art where art.id IN (SELECT DISTINCT(inv.code_art) FROM TB_Inventario inv WHERE inv.code_bod IN (" + sb.toString() + "))";
                ResultSet rs = stm.executeQuery(sql);
                while (rs.next()) {
                    a = processor.process(rs, 0);
                    if (a != null) articulos.add(a);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return articulos;
        }
    }
    public class IntermdiateArticuuloUpdate implements IntermediateUpdate<Articulo, String> {
        @Override
        public boolean updateSingle(Articulo reference) {
            String sql = "UPDATE TB_Articulo SET cost = ? , util = ?, grav = ?, TB_Articulo.desc = ? WHERE id = ?";
            try (Connection c = DataBase.getInstance().getConnection()) {
                PreparedStatement stm = c.prepareStatement(sql);
                stm.setDouble(1,reference.getCosto());
                stm.setDouble(2,reference.getUtilidad());
                int b = reference.isEsGrabado() ? 1 : 0 ;
                stm.setInt(3,b);
                stm.setString(4,reference.getDescripcion());
                stm.setString(5,reference.getCodigo());
                return stm.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        public boolean updateCollection(Collection<Articulo> collection) {
            boolean flag = false;
            String sql = "UPDATE TB_Articulo SET cost = ? , util = ?, grav = ?, TB_Articulo.desc = ? WHERE id = ?";
            try (Connection c = DataBase.getInstance().getConnection()) {
                PreparedStatement stm = c.prepareStatement(sql);
                for(Articulo reference : collection ) {
                    stm.setDouble(1, reference.getCosto());
                    stm.setDouble(2, reference.getUtilidad());
                    int b = reference.isEsGrabado() ? 1 : 0;
                    stm.setInt(3, b);
                    stm.setString(4, reference.getDescripcion());
                    stm.setString(5, reference.getCodigo());
                    if(stm.executeUpdate() > 0) flag = true;
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        public boolean updateIf(Articulo ref, Predicate<Articulo>... what) {
            if(what.length<1) return updateSingle(ref);
            Predicate<Articulo> ini = what[0];
            for(int i = 1; i<what.length; i++) ini = ini.and(what[i]);
             return  ini.test(ref)? updateSingle(ref) : false;
        }

        @Override
        public boolean updateCollectionWhen(Collection<Articulo> refs, Predicate<Articulo>... what) {
            if(what.length < 1) return updateCollection(refs);
            Predicate<Articulo> ini = what[0];
            for(int i = 1; i<what.length; i++) ini = ini.and(what[i]);
            List<Articulo> oks = refs.stream().filter(ini).collect(Collectors.toList());
            return updateCollection(oks);
        }
    }
    public class IntermediateArticuloInsert implements IntermediateInsert<Articulo>{
        @Override
        public boolean single(Articulo ref) {
            try(Connection c = DataBase.getInstance().getConnection()){
                int isg = ref.isEsGrabado()?1:0;
                String cod = Dataformat.asSqlString(ref.getCodigo());
                String des = Dataformat.asSqlString(ref.getDescripcion());
                String sql = "INSERT INTO TB_Articulo VALUES (".concat(cod+", ").concat(des+", ")+ ref.getCosto() +", "+ref.getUtilidad()+ ", "+ isg +")";
                Statement st = c.createStatement();
                return  st.executeUpdate(sql) > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        public boolean aCollection(Collection<Articulo> refs) {
            boolean flag = false;
            try(Connection c = DataBase.getInstance().getConnection()){
                for(Articulo ref : refs) {
                    int isg = ref.isEsGrabado() ? 1 : 0;
                    String cod = Dataformat.asSqlString(ref.getCodigo());
                    String des = Dataformat.asSqlString(ref.getDescripcion());
                    String sql = "INSERT INTO TB_Articulo VALUES (".concat(cod + ", ").concat(des + ", ") + ref.getCosto() + ", " + ref.getUtilidad() + ", " + isg + ")";
                    Statement st = c.createStatement();
                    if(st.executeUpdate(sql)>0) flag = true;
                }
                return  flag;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        public boolean aCollectionWhere(Collection<Articulo> refs, Predicate<Articulo>... conds) {
            boolean flag = false;
            if(conds.length > 0) {
                Predicate<Articulo> acc = conds[0];
                for(int i = 1; i<conds.length ; i++) acc = acc.and(conds[i]);
                refs = refs.stream().filter(acc).collect(Collectors.toList());
            }
            try(Connection c = DataBase.getInstance().getConnection()){
                for(Articulo ref : refs) {
                    int isg = ref.isEsGrabado() ? 1 : 0;
                    String cod = Dataformat.asSqlString(ref.getCodigo());
                    String des = Dataformat.asSqlString(ref.getDescripcion());
                    String sql = "INSERT INTO TB_Articulo VALUES (".concat(cod + ", ").concat(des + ", ") + ref.getCosto() + ", " + ref.getUtilidad() + ", " + isg + ")";
                    Statement st = c.createStatement();
                    if(st.executeUpdate(sql)>0) flag = true;
                }
                return  flag;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        }
    }
    public class IntermediateArticuloDelete implements IntermediateDelete<Articulo>{
        @Override
        public void single(Articulo ref) {
            try (Connection c = DataBase.getInstance().getConnection()){
                Statement stm = c.createStatement();
                String sql = "DELETE FROM TB_Articulo WHERE id = " + Dataformat.asSqlString(ref.getCodigo());
                stm.executeUpdate(sql);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void aCollection(Collection<Articulo> refs) {
            try (Connection c = DataBase.getInstance().getConnection()){
                Statement stm = c.createStatement();
                for(Articulo ref : refs) {
                    String sql = "DELETE FROM TB_Articulo WHERE id = " + Dataformat.asSqlString(ref.getCodigo());
                    stm.executeUpdate(sql);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void aCollectionWhere(Collection<Articulo> refs, Predicate<Articulo>... conds) {
            if(conds.length > 0) {
                Predicate<Articulo> acc = conds[0];
                for(int i = 1; i<conds.length ; i++) acc = acc.and(conds[i]);
                refs = refs.stream().filter(acc).collect(Collectors.toList());
            }
            try (Connection c = DataBase.getInstance().getConnection()){
                Statement stm = c.createStatement();
                for(Articulo ref : refs) {
                    String sql = "DELETE FROM TB_Articulo WHERE id = " + Dataformat.asSqlString(ref.getCodigo());
                    stm.executeUpdate(sql);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

}
