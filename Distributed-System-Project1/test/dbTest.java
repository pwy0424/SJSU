import org.junit.Test;
import gash.router.database.*;

public class dbTest {

	@Test
	public void testmyDb() throws Exception{
	 DatabaseService dbs= DatabaseService.getInstance();
	 dbs.dbConfiguration("postgresql","jdbc:postgresql://127.0.0.1:5432/postgres", "postgres", "test");
	 DatabaseClient dbc= dbs.getDb();
	 dbc.getMessage("1");
	}
	
}

