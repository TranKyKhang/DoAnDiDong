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

export const banUser= async (req,res)=>{
     console.log("BODY:", req.body);
    console.log("PARAMS:", req.params);
    try{
        const { communityId } = req.params; 
        const { userId, currentUserId } = req.body; 

    if (!currentUserId) {
      return res.status(400).json({ success: false, message: "currentUserId bắt buộc" });
    }

    
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
    console.log("data : ",roleRows[0]);
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
    }catch(err){
        console.error("MYSQL ERROR:", err);
    res.status(500).json({ success: false, message: err.message });
    }
}

export const unBanUser= async (req,res)=>{
     console.log("BODY:", req.body);
    console.log("PARAMS:", req.params);
    try{
        const { communityId } = req.params; 
        const { userId, currentUserId } = req.body; 

    if (!currentUserId) {
      return res.status(400).json({ success: false, message: "currentUserId bắt buộc" });
    }

    
    const [communityRows] = await db.query(
      "SELECT id FROM COMMUNITIES WHERE id = ?", [communityId]
    );
    if (communityRows.length === 0) {
      return res.status(404).json({ success: false, message: "Community không tồn tại" });
    }

   
    const [userRows] = await db.query("SELECT id FROM USERS WHERE id = ?", [userId]);
    if (userRows.length === 0) {
      return res.status(405).json({ success: false, message: "User muốn unban không tồn tại" });
    }

    
    const [roleRows] = await db.query(
      "SELECT role FROM users_communities WHERE community_id = ? AND user_id = ?",
      [communityId, currentUserId]
    );
    console.log("data : ",roleRows[0]);
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
    }catch(err){
        console.error("MYSQL ERROR:", err);
    res.status(500).json({ success: false, message: err.message });
    }
}

export const changeRole = async (req, res) => {
    try {
    const { communityId } = req.params;
    const { currentUserId, targetUserId, role } = req.body;

    if (!currentUserId || !targetUserId || !role) {
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
