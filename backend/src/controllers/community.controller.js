import db from "../config/db.js";

export const getAllCommunity = async (req, res) => {
  try {
    const [rows] = await db.query("SELECT * FROM COMMUNITIES");
    return res.json({
      success: true,
      data: rows
    });
  } catch (err) {
    console.error("MYSQL ERROR:", err);
    return res.status(500).json({
      success: false,
      message: err.message
    });
  }
};

export const banUser = async (req, res) => {
    console.log("BODY:", req.body);
    console.log("PARAMS:", req.params);
    try {
        const { communityId } = req.params;
        const { userId } = req.body; 

        if (!req.user?.id) {
            return res.status(401).json({ success: false, message: "Token không hợp lệ" });
        }
        const currentUserId = req.user.id; 

       
        const [communityRows] = await db.query(
            "SELECT id FROM COMMUNITIES WHERE id = ?", [communityId]
        );
        if (communityRows.length === 0) {
            return res.status(404).json({ success: false, message: "Community không tồn tại" });
        }

        
        const [userRows] = await db.query("SELECT id FROM USERS WHERE id = ?", [userId]);
        if (userRows.length === 0) {
            return res.status(405).json({ success: false, message: "User muốn ban không tồn tại" });
        }

        
        const [roleRows] = await db.query(
            "SELECT role FROM users_communities WHERE community_id = ? AND user_id = ?",
            [communityId, currentUserId]
        );
        console.log("data : ", roleRows[0]);
        if (roleRows.length === 0 || !['admin', 'moderator'].includes(roleRows[0].role)) {
            return res.status(403).json({ success: false, message: "Bạn không có quyền ban user này" });
        }

        
        await db.query(
            "UPDATE users_communities SET is_banned = 1 WHERE community_id = ? AND user_id = ?",
            [communityId, userId]
        );

        res.json({
            success: true,
            message: `User ${userId} đã bị ban trong community ${communityId}`
        });
    } catch (err) {
        console.error("MYSQL ERROR:", err);
        res.status(500).json({ success: false, message: err.message });
    }
};


export const unBanUser = async (req, res) => {
    console.log("BODY:", req.body);
    console.log("PARAMS:", req.params);
    try {
        const { communityId } = req.params;
        const { userId } = req.body;

        if (!req.user?.id) {
            return res.status(401).json({ success: false, message: "Token không hợp lệ" });
        }
        const currentUserId = req.user.id;

        const [communityRows] = await db.query(
            "SELECT id FROM COMMUNITIES WHERE id = ?", [communityId]
        );
        if (communityRows.length === 0) {
            return res.status(404).json({ success: false, message: "Community không tồn tại" });
        }

        const [userRows] = await db.query(
            "SELECT id FROM USERS WHERE id = ?", [userId]
        );
        if (userRows.length === 0) {
            return res.status(405).json({ success: false, message: "User muốn unban không tồn tại" });
        }

        const [roleRows] = await db.query(
            "SELECT role FROM users_communities WHERE community_id = ? AND user_id = ?",
            [communityId, currentUserId]
        );
        console.log("data: ", roleRows[0]);
        if (roleRows.length === 0 || !['admin', 'moderator'].includes(roleRows[0].role)) {
            return res.status(403).json({ success: false, message: "Bạn không có quyền unban user này" });
        }

        await db.query(
            "UPDATE users_communities SET is_banned = 0 WHERE community_id = ? AND user_id = ?",
            [communityId, userId]
        );

        res.json({
            success: true,
            message: `User ${userId} đã unban trong community ${communityId}`
        });

    } catch (err) {
        console.error("MYSQL ERROR:", err);
        res.status(500).json({ success: false, message: err.message });
    }
};


export const changeRole = async (req, res) => {
  try {
    const { communityId } = req.params;
    const { targetUserId, role } = req.body;
    const currentUserId = req.user.id; 

    if (!targetUserId || !role) {
      return res.status(400).json({
        success: false,
        message: "Thiếu dữ liệu"
      });
    }

    if (role === "admin") {
      return res.status(403).json({
        success: false,
        message: "Không thể cấp quyền admin"
      });
    }

    if (!["moderator", "member"].includes(role)) {
      return res.status(400).json({
        success: false,
        message: "Role không hợp lệ"
      });
    }


    const [actorRows] = await db.query(
      `SELECT role FROM users_communities
       WHERE community_id = ? AND user_id = ?`,
      [communityId, currentUserId]
    );

    if (actorRows.length === 0) {
      return res.status(403).json({
        success: false,
        message: "Bạn không thuộc community"
      });
    }

    if (!["admin", "moderator"].includes(actorRows[0].role)) {
      return res.status(403).json({
        success: false,
        message: "Bạn không có quyền"
      });
    }


    const [targetRows] = await db.query(
      `SELECT role FROM users_communities
       WHERE community_id = ? AND user_id = ?`,
      [communityId, targetUserId]
    );

    if (targetRows.length === 0) {
      return res.status(404).json({
        success: false,
        message: "User không thuộc community"
      });
    }

    const actorRole = actorRows[0].role;
    const targetRole = targetRows[0].role;

    if (
      actorRole === "moderator" &&
      !(targetRole === "member" && role === "moderator")
    ) {
      return res.status(403).json({
        success: false,
        message: "Moderator chỉ được nâng member lên moderator"
      });
    }

    await db.query(
      `UPDATE users_communities
       SET role = ?
       WHERE community_id = ? AND user_id = ?`,
      [role, communityId, targetUserId]
    );

    res.json({
      success: true,
      message: `Đã cập nhật role thành ${role}`
    });
  } catch (err) {
    console.error("MYSQL ERROR:", err);
    res.status(500).json({
      success: false,
      message: err.message
    });
  }
};




// Huy
// Tao cong dong
export const createCommunities = async (req, res) => {
    // Lay du lieu tu client
    const { name, description } = req.body;
    const user_id=req.user.id;
    if (!name || !user_id) {
        return res.status(400).json({ message: "Tên cộng đồng và ID người tạo là bắt buộc!" });
    }
    try {
        const checkQuery = 'SELECT id FROM communities WHERE name = ?';
        const [existing] = await db.query(checkQuery, [name]);

        if (existing.length > 0) {
            return res.status(409).json({ message: "Tên cộng đồng này đã tồn tại!" });
        }

        // 1. Them cong dong moi vao DB
        const insertQuery = `
            INSERT INTO communities (name, description, user_id) 
            VALUES (?, ?, ?)
        `;
        const [result] = await db.query(insertQuery, [name, description, user_id]); 
        const newCommunityId = result.insertId; 

        await db.query(
            'INSERT INTO users_communities (community_id, user_id,role) VALUES (?, ?,"moderator"))', 
            [newCommunityId, user_id]
        );

        // Tra ve ket qua thanh cong
        return res.status(201).json({
            message: "Tạo cộng đồng thành công",
            communityId: newCommunityId,
            data: {
                name: name,
                description: description
            }
        });

    } catch (error) {
        console.error("Lỗi tạo tự động: ", error);
        return res.status(500).json({ message: "Lỗi Server", error: error.message });
    }
};

// Hàm lấy danh sách cộng đồng mà user đã tham gia
export const getCommunitiesByUser = async (req, res) => {
    const userId = req.user.id;

    if (!userId) {
        return res.status(400).json({ message: "Thiếu User ID" });
    }

    try {
        //Kết 2 bảng để lấy thông tin cộng đồng + role của user
        const sql = `
            SELECT c.*, uc.role 
            FROM communities c
              JOIN users_communities uc ON c.id = uc.community_id
            WHERE uc.user_id = ?
        `;

        const [rows] = await db.query(sql, [userId]);

        return res.status(200).json({
            message: "Lấy danh sách cộng đồng của user thành công",
            data: rows
        });
    } catch (error) {
        console.error("Lỗi lấy danh sách theo user: ", error);
        return res.status(500).json({ message: "Lỗi Server", error: error.message });
    }
};
// Tham gia cong dong
export const joinCommunity=async(req,res)=>{
  //Lay user tam thoi 
  const {community_id}=req.body;
  const user_id=req.user.id;
  if(!user_id||!community_id){
    return res.status(400).json({message:"Thiếu user_id hoặc community_id !"});
  }
  try{
    const checkQuery = "SELECT * FROM users_communities WHERE user_id = ? AND community_id = ?";
    const [existing] = await db.query(checkQuery, [user_id, community_id]);
    if (existing.length > 0) {
      return res.status(409).json({ message: "Bạn đã tham gia cộng đồng này rồi!" });
    }

    // Thêm vào bảng (Mặc định role là Member)
    const insertQuery = "INSERT INTO users_communities (user_id, community_id, role) VALUES (?, ?, 'member')";
    if (existing.length > 0) {
      return res.status(409).json({ message: "Bạn đã tham gia cộng đồng này rồi!" });
    }
    await db.query(insertQuery, [user_id, community_id]);
    return res.status(200).json({ message: "Tham gia thành công!" });
    
  }catch(error){
    console.error("Lỗi join community: ", error);
    return res.status(500).json({ message: "Lỗi Server", error: error.message });
  }

};
//Roi cong dong
export const leaveCommunity = async (req, res) => {
    const {  community_id } = req.body;
    const user_id=req.user.id;
    if (!user_id || !community_id) {
        return res.status(400).json({ message: "Thiếu user_id hoặc community_id" });
    }

    try {
      const deleteQuery = "DELETE FROM users_communities WHERE user_id = ? AND community_id = ?";
      const [result] = await db.query(deleteQuery, [user_id, community_id]);

      // Kiểm tra xem có xóa được dòng nào không
      if (result.affectedRows === 0) {
        return res.status(404).json({ message: "Bạn chưa tham gia cộng đồng này nên không thể rời!" });
      }
      return res.status(200).json({ message: "Đã rời cộng đồng thành công." });

    }catch (error) {
      console.error("Lỗi leave community: ", error);
      return res.status(500).json({ message: "Lỗi Server", error: error.message });
    }
};

//Xem thong tin cong dong 
  export const getCommunityDetails=async(req,res)=>{
    const id = req.params.id;
    if(!id) return res.status(400).json({message:"Thiếu trường id !"});
    try{
      const details="SELECT * FROM communities WHERE id=?";
      const [result] = await db.query(details, [id]);
      return res.status(200).json({
        message: "Lấy thông tin cộng đồng thành công",
        data: result[0]
      });

    } catch (error) {
        console.error("Lỗi lấy thông tin cộng đồng: ", error);
        return res.status(500).json({ message: "Lỗi Server", error: error.message });
      }
  };
  //Danh sach bai viet theo tung cong dong
  export const getPostCommunityByID=async(req,res)=>{
    const community_id = req.params.community_id;
    if(!community_id) return res.status(400).json({message:"Thiếu trường id !"});
    try{
      const sql = `
        SELECT p.*, u.username, u.avatar 
        FROM posts p
          JOIN users u ON p.user_id = u.id
        WHERE p.community_id = ?
        ORDER BY p.created_at DESC
      `;
      const [result] = await db.query(sql, [community_id]);
      return res.status(200).json({
        message: "Lấy thông tin danh sách bài viết thành công !",
        count: result.length,
        data: result
      });

    } catch (error) {
        console.error("Lỗi lấy thông tin danh sách bài viết: ", error);
        return res.status(500).json({ message: "Lỗi Server", error: error.message });
      }
  };

export const searchCommunities = async (req, res) => {
    try {
        const { q } = req.query;

        if (!q || q.trim().length === 0) {
            return res.json({ success: true, data: [] });
        }

        const sql = `
            SELECT 
                id, 
                name,
                icon
            FROM communities
            WHERE name LIKE ?
            ORDER BY 
                CASE 
                    WHEN name = ? THEN 1      -- Exact match gets top priority
                    WHEN name LIKE ? THEN 2   -- "Starts with" gets second priority
                    ELSE 3                    -- "Contains" gets third priority
                END,
                name ASC
            LIMIT 10`;

        const exactMatch = q;
        const startsWith = `${q}%`;
        const contains = `%${q}%`;

        const [results] = await db.query(sql, [contains, exactMatch, startsWith]);

        res.json({
            success: true,
            data: results
        });
    } catch (err) {
        console.error(err);
        res.status(500).json({ success: false, message: err.message });
    }
};
