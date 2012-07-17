package withings.apiaccess;

public class WithingsUser {
	public long id;
	public String name;
	public String publicKey;
	public int isPublic;
	
	public WithingsUser(long id, String name, String publicKey, int isPublic) {
		this.id = id;
		this.name = name;
		this.publicKey = publicKey;
		this.isPublic = isPublic;
	}
	
	@Override
	public String toString() {
		return name + "; id: " + id + "; public key: " + publicKey + "; isPublic: " + isPublic;
	}
}
