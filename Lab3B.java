/* 
 * CS61 Lab3 Part B Daniel Kim
 */

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import com.mongodb.Block;
import com.mongodb.client.MongoCursor;
import static com.mongodb.client.model.Filters.*;
import com.mongodb.client.result.DeleteResult;
import static com.mongodb.client.model.Updates.*;
import com.mongodb.client.result.UpdateResult;

import java.util.*;
import java.util.regex.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

public class Lab3B {

	public static void main(String[] args) {
		MongoClientURI uri = new MongoClientURI(
	   "mongodb://Team04:ktjZxAsiQG1Yu6Nu@cluster0-shard-00-00-ppp7l.mongodb.net:27017,cluster0-shard-00-01-ppp7l.mongodb.net:27017,cluster0-shard-00-02-ppp7l.mongodb.net:27017/Team04DB?ssl=true&replicaSet=Cluster0-shard-0&authSource=admin");

		MongoClient mongoClient = new MongoClient(uri);
		MongoDatabase db = mongoClient.getDatabase("Team04DB");
		MongoCollection<Document> coll = db.getCollection("blogsCollection");

		Scanner scan = new Scanner(System.in);
		String input;

		System.out.println("Starting blog engine...\n");

		while(scan.hasNextLine()) {
			input = scan.nextLine();
			List<String> command = new ArrayList<String>();
			Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(input);
			while (m.find()) {
				command.add(m.group(1).replace("\"", "")); 	// Add .replace("\"", "") to remove surrounding quotes.
			}

			switch(command.get(0)) {
				case "post": post(coll, command); break;
				case "comment": comment(coll, command); break;
				case "delete": delete(coll, command); break;
				case "show": show(coll, command); break;
				default: System.out.println("Not a valid operation\n"); break;
			}
		}
	}

	// post blogName userName title postBody tags 
	public static void post(MongoCollection<Document> coll, List<String> command) {
		
		// Generate new entryID
		int entryID = (int)coll.count() + 1;

		// Get date time
		DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
		LocalDateTime dateTime = LocalDateTime.now();
		String formattedDateTime = dateTime.format(formatter);

		// Insert Document
		Document post = new Document("entryID", entryID)
			.append("blogName", command.get(1))
			.append("userName", command.get(2))
			.append("title", command.get(3))
			.append("body", command.get(4))
			.append("entryDate", formattedDateTime);

		// Check for tags
		if (command.size() > 5) {
			String [] tags = command.get(5).split(",");
			List<String> taglist = Arrays.asList(tags);
			post.append("tags", taglist);
		}

		coll.insertOne(post);
	}

	// comment blogname entryID userName commentBody
	public static void comment(MongoCollection<Document> coll, List<String> command) {
		
		// Check that referenced post/comment exists
		if (Integer.parseInt(command.get(2)) > coll.count()) {
			System.out.println("Cannot comment on post/comment that doesn't exist\n");
		} else {

			// Generate new entryID
			int entryID = (int)coll.count() + 1;

			// Get date time
			DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
			LocalDateTime dateTime = LocalDateTime.now();
			String formattedDateTime = dateTime.format(formatter);
		
			// Insert Document
			Document comment = new Document("entryID", entryID)
				.append("blogName", command.get(1))
				.append("targetEntryID", Integer.parseInt(command.get(2)))
				.append("userName", command.get(3))
				.append("body", command.get(4))
				.append("entryDate", formattedDateTime);

			coll.insertOne(comment);
		}
	}

	// delete blogname entryID userName
	public static void delete(MongoCollection<Document> coll, List<String> command) {
		
		// Check that referenced post/comment exists
		if (Integer.parseInt(command.get(2)) > coll.count()) {
			System.out.println("Cannot delete post/comment that doesn't exist\n");
		} else if (coll.find(and(eq("blogName", command.get(1)), eq("entryID", Integer.parseInt(command.get(2))))).first() == null) {
			System.out.println("Cannot delete post/comment because it is not in the blog\n");
		} else {	//
			// Get date time
			DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
			LocalDateTime dateTime = LocalDateTime.now();
			String formattedDateTime = dateTime.format(formatter);

			String msg = "Deleted by " + command.get(3) + " on " + formattedDateTime; 
			coll.updateOne(eq("entryID", Integer.parseInt(command.get(2))), new Document("$set", new Document("body", msg)));
		}
	}

	// show blogName
	public static void show(MongoCollection<Document> coll, List<String> command) {

		System.out.println(command.get(1));
		System.out.println("----------------------------------------------");

		// Find matching posts
		MongoCursor<Document> cursor = coll.find(and(eq("blogName", command.get(1)), (not(exists("targetEntryID"))))).iterator();

		try {
			while (cursor.hasNext()) {
				Document post = cursor.next();

				String block = "Post - ";
				String entryDate = (String) post.get("entryDate");
				block = block + entryDate + "\n";
				String entryID = post.get("entryID").toString();
				block = block + " (" + entryID + ") ";
				String userName = (String) post.get("userName");
				block = block + userName + "\n ";
				String title = (String) post.get("title");
				block = block + title + "\n ";

				if (post.containsKey("tags")) {
					String tags = post.get("tags").toString();
					block = block + "tags: " + tags + "\n ";
				}

				String body = (String) post.get("body");
				block = block + body + "\n";

				System.out.println(block);

				show_comments(coll, Integer.parseInt(entryID), 0);
			}
		} finally {
			cursor.close();
		}
	}

	public static void show_comments(MongoCollection<Document> coll, int targetID, int layer) {
		// Find matching comments
		MongoCursor<Document> cursor = coll.find(eq("targetEntryID", targetID)).iterator();

		try {
			while (cursor.hasNext()) {
				Document comment = cursor.next();

				String block = "Comment - ";
				String entryDate = (String) comment.get("entryDate");
				block = block + entryDate + "\n";
				String entryID = comment.get("entryID").toString();
				block = block + " (" + entryID + ") ";
				String userName = (String) comment.get("userName");
				block = block + userName + "\n ";
				String body = (String) comment.get("body");
				block = block + body + "\n";

				// Indent
				String indent = "\t";
				for (int i = 0; i < layer; i++) {
					indent += "\t";
				}

				String indented = block.replaceAll("(?m)^", indent);
				System.out.println(indented);

				int new_layer = layer + 1;
				show_comments(coll, Integer.parseInt(entryID), new_layer);
			}
		} finally {
			cursor.close();
		}
	}
}
